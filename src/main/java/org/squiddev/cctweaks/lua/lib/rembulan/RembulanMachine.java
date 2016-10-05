package org.squiddev.cctweaks.lua.lib.rembulan;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.lua.ILuaMachine;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.exec.CallException;
import net.sandius.rembulan.exec.CallPausedException;
import net.sandius.rembulan.exec.Continuation;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.DefaultTable;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.lib.ModuleLib;
import net.sandius.rembulan.lib.impl.*;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.*;
import org.squiddev.cctweaks.api.lua.ArgumentDelegator;
import org.squiddev.cctweaks.lua.Config;
import org.squiddev.cctweaks.lua.StreamHelpers;
import org.squiddev.cctweaks.lua.ThreadBuilder;
import org.squiddev.cctweaks.lua.lib.AbstractLuaContext;
import org.squiddev.cctweaks.lua.lib.BinaryConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.squiddev.cctweaks.lua.lib.LuaMachineHelpers.ILLEGAL_NAMES;
import static org.squiddev.cctweaks.lua.lib.LuaMachineHelpers.getHost;

public class RembulanMachine extends AbstractLuaContext implements ILuaMachine {
	private static final ScheduledExecutorService threads = ThreadBuilder.createThread("Rembulan", 128);

	private final Computer computer;
	private final StateContext state;
	private final Table globals;
	private final DirectCallExecutor executor = DirectCallExecutor.newExecutorWithTickLimit(100000);

	private String eventFilter = null;
	private String hardAbort = null;
	private String softAbort = null;
	private Continuation continuation;

	public RembulanMachine(Computer computer) {
		super(computer);
		this.computer = computer;

		StateContext state = this.state = StateContexts.newDefaultInstance();

		Table globals = this.globals = state.newTable();
		new DefaultBasicLib(null, null, globals).installInto(state, globals);

		// TODO: Correctly install modules
		ModuleLib moduleLib = new DefaultModuleLib(state, globals);
		moduleLib.installInto(state, globals);

		moduleLib.install(new DefaultCoroutineLib());
		moduleLib.install(new DefaultStringLib());
		moduleLib.install(new DefaultMathLib());
		moduleLib.install(new DefaultTableLib());
		moduleLib.install(new DefaultUtf8Lib());
		moduleLib.install(new DefaultDebugLib());

		for (String global : ILLEGAL_NAMES) {
			globals.rawset(global, null);
		}

		String host = getHost(computer);
		if (host != null) globals.rawset("_HOST", host);

		globals.rawset("_CC_VERSION", ComputerCraft.getVersion());
		globals.rawset("_MC_VERSION", Config.mcVersion);
		globals.rawset("_LUAJ_VERSION", "Rembulan 0.1");
		if (ComputerCraft.disable_lua51_features) {
			globals.rawset("_CC_DISABLE_LUA51_FEATURES", true);
		}
	}

	@Override
	public Object[] yield(Object[] objects) throws InterruptedException {
		return new Object[0];
	}

	@Override
	public void addAPI(ILuaAPI api) {
		Table table = wrapLuaObject(api);
		for (String name : api.getNames()) {
			globals.rawset(name, table);
		}
	}

	private Table wrapLuaObject(final ILuaObject object) {
		String[] methods = object.getMethodNames();
		Table result = DefaultTable.factory().newTable(0, methods.length);

		for (int i = 0; i < methods.length; i++) {
			final int method = i;
			result.rawset(methods[i], new AbstractFunctionAnyArg() {
				@Override
				public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
					if (!Config.Computer.timeoutError) {
						String message = softAbort;
						if (message != null) {
							softAbort = null;
							hardAbort = null;
							throw new LuaRuntimeException(message);
						}
					}

					try {
						// TODO: Argument delegation
						Object[] results = ArgumentDelegator.delegateLuaObject(object, RembulanMachine.this, method, new RembulanArguments(args));
						context.getReturnBuffer().setToContentsOf(results);
					} catch (LuaException e) {
						throw new LuaRuntimeException(e.getMessage());
					} catch (InterruptedException e) {
						try {
							context.yield(args);
						} catch (UnresolvedControlThrowable ct) {
							throw ct.resolve(this, null);
						}
					} catch (Throwable e) {
						throw new LuaRuntimeException("Java Exception Thrown: " + e.toString());
					}
				}

				@Override
				public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {

				}
			});
		}

		return result;
	}

	@Override
	public void loadBios(InputStream inputStream) {
		if (continuation != null) return;
		try {
			ChunkLoader loader = CompilerChunkLoader.of("/");
			LuaFunction function = loader.loadTextChunk(new Variable(globals), "bios.lua", StreamHelpers.toString(inputStream));
		} catch (LuaRuntimeException e) {
			continuation = null;
		} catch (IOException e) {
			continuation = null;
		} catch (LoaderException e) {
			continuation = null;
		}
	}

	@Override
	public void handleEvent(String eventName, Object[] arguments) {
		if (continuation == null) return;

		if (eventFilter == null || eventName == null || eventName.equals(eventFilter) || eventName.equals("terminate")) {
			try {
				Object[] args = null;
				if (eventName != null) {
					Object[] params = toValues(arguments);
					if (params.length == 0) {
						args = new Object[]{eventName};
					} else {
						args = new Object[params.length + 1];
						args[0] = eventName;
						System.arraycopy(params, 0, args, 1, params.length);
					}
				}

				Object[] results = null;
				try {

					results = executor.resume(continuation);
				} catch (CallPausedException e) {
					continuation = e.getContinuation();
				} catch (CallException e) {
					if (hardAbort != null) throw new LuaRuntimeException(hardAbort);
					throw new LuaRuntimeException(e.getMessage());
				} catch (InterruptedException e) {
					if (hardAbort != null) throw new LuaRuntimeException(hardAbort);
					throw new LuaRuntimeException(e.getMessage());
				}

				if (hardAbort != null) throw new LuaRuntimeException(hardAbort);
				Object filter = results[0];
				if (filter instanceof String) {
					eventFilter = filter.toString();
				} else {
					eventFilter = null;
				}
			} catch (LuaRuntimeException e) {
				continuation = null;
			} finally {
				softAbort = null;
				hardAbort = null;
			}

		}
	}

	@Override
	public void softAbort(String message) {
		softAbort = message;
	}

	@Override
	public void hardAbort(String message) {
		softAbort = message;
		hardAbort = message;
	}

	@Override
	public boolean saveState(OutputStream outputStream) {
		return false;
	}

	@Override
	public boolean restoreState(InputStream inputStream) {
		return false;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public void unload() {

	}

	private Object toValue(Object object, Map<Object, Table> tables) {
		if (object == null || object instanceof Number || object instanceof Boolean || object instanceof String) {
			return object;
		} else if (object instanceof byte[]) {
			return BinaryConverter.decodeString((byte[]) object);
		} else if (object instanceof Map) {
			if (tables == null) {
				tables = new IdentityHashMap<Object, Table>();
			} else {
				Table value = tables.get(object);
				if (value != null) return value;
			}

			Table table = new DefaultTable();
			tables.put(object, table);

			for (Map.Entry<?, ?> pair : ((Map<?, ?>) object).entrySet()) {
				Object key = toValue(pair.getKey(), tables);
				Object value = toValue(pair.getValue(), tables);
				if (key != null && value != null) {
					table.rawset(key, value);
				}
			}

			return table;
		} else if (object instanceof ILuaObject) {
			return wrapLuaObject((ILuaObject) object);
		} else {
			return null;
		}
	}

	private Object[] toValues(Object[] objects) {
		if (objects != null && objects.length != 0) {
			Object[] values = new Object[objects.length];

			for (int i = 0; i < objects.length; ++i) {
				Object object = objects[i];
				values[i] = toValue(object, null);
			}

			return values;
		} else {
			return objects;
		}
	}
}
