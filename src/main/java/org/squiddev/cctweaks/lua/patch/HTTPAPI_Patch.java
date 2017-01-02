package org.squiddev.cctweaks.lua.patch;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.HTTPAPI;
import dan200.computercraft.core.apis.IAPIEnvironment;
import org.squiddev.cctweaks.lua.lib.HTTPRequest;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Patches the HTTP API with several features:
 * - HTTP response on failure
 * - Additional "fetch" method which isn't wrapped in the bios.
 */
public class HTTPAPI_Patch extends HTTPAPI {
	@MergeVisitor.Stub
	private List<HTTPRequest> m_httpRequests;
	@MergeVisitor.Stub
	private IAPIEnvironment m_apiEnvironment;

	@MergeVisitor.Stub
	public HTTPAPI_Patch(IAPIEnvironment environment) {
		super(environment);
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"request", "fetch", "checkURL"};
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException {
		switch (method) {
			case 0:  // request
			case 1: {
				if (args.length < 1 || !(args[0] instanceof String)) {
					throw new LuaException("Expected string");
				}

				String urlString = args[0].toString();
				String data = args.length > 1 && args[1] instanceof String ? (String) args[1] : null;
				String verb = args.length > 3 && args[3] instanceof String ? (String) args[3] : null;

				HashMap<String, String> headers = null;
				if (args.length >= 3 && args[2] instanceof Map) {
					Map<?, ?> argHeader = (Map<?, ?>) args[2];
					headers = new HashMap<String, String>(argHeader.size());

					for (Object key : argHeader.keySet()) {
						Object value = argHeader.get(key);
						if (key instanceof String && value instanceof String) {
							headers.put((String) key, (String) value);
						}
					}
				}

				try {
					HTTPRequest request = new HTTPRequest(urlString, data, headers, verb);
					synchronized (this.m_httpRequests) {
						this.m_httpRequests.add(request);
					}

					return new Object[]{Boolean.valueOf(true)};
				} catch (LuaException e) {
					return new Object[]{Boolean.valueOf(false), e.getMessage()};
				}
			}
			case 2: { // Check URL
				if (args.length < 1 || !(args[0] instanceof String)) {
					throw new LuaException("Expected string");
				}
				String urlString = args[0].toString();

				try {
					HTTPRequest.checkURL(urlString);
					return new Object[]{Boolean.valueOf(true)};
				} catch (LuaException e) {
					return new Object[]{Boolean.valueOf(false), e.getMessage()};
				}
			}
			default:
				return null;
		}
	}

	@Override
	public void advance(double dt) {
		synchronized (m_httpRequests) {
			Iterator<HTTPRequest> it = m_httpRequests.iterator();
			while (it.hasNext()) {
				HTTPRequest h = it.next();
				if (h.isComplete()) {
					String url = h.getURL();
					if (h.wasSuccessful()) {
						m_apiEnvironment.queueEvent("http_success", new Object[]{url, h.asResponse()});
					} else {
						m_apiEnvironment.queueEvent("http_failure", new Object[]{url, "Could not connect", h.asResponse()});
					}
					it.remove();
				}
			}
		}
	}
}
