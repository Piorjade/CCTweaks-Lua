package org.squiddev.cctweaks.lua.lib.rembulan;

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.lib.impl.StandardLibrary;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.LuaFunction;

public class RembulanExperiment {
	public static void main(String[] args) throws LoaderException {
		String program = "local res = {false} while true do res = {coroutine.yield(res)} end";

		StateContext state = StateContexts.newDefaultInstance();
		Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);

		ChunkLoader loader = CompilerChunkLoader.of("infinite_loop");
		LuaFunction main = loader.loadTextChunk(new Variable(env), "loop", program);

		// execute at most one million ops
		DirectCallExecutor executor = DirectCallExecutor.newExecutorWithTickLimit(1000000);

		try {
			executor.call(state, main);
			throw new AssertionError();  // never reaches this point
		} catch (Exception ex) {
			// Attempt to yield outside a coroutine
			System.out.println("n = " + env.rawget("n"));
		}
	}
}
