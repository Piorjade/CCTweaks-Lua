package org.squiddev.cctweaks.lua.lib.socket;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import org.squiddev.cctweaks.lua.TweaksLogger;
import org.squiddev.cctweaks.lua.lib.LuaHelpers;

import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;

public class WebSocketConnection extends AbstractConnection {
	private static final Object lock = new Object();
	private static TrustManagerFactory trustManager;

	private Map<String, String> headers = Collections.emptyMap();
	private final Queue<ByteBuf> streams = new ArrayDeque<ByteBuf>();

	private ClientListener clientListener;
	private ChannelFuture channelFuture;

	public WebSocketConnection(SocketAPI owner, IComputerAccess computer, int id) throws IOException {
		super(owner, computer, id);
	}

	public static TrustManagerFactory getTrustManager() {
		if (trustManager != null) return trustManager;
		synchronized (lock) {
			if (trustManager != null) return trustManager;

			TrustManagerFactory tmf = null;
			try {
				tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init((KeyStore) null);
			} catch (Exception e) {
				TweaksLogger.error("Cannot setup trust manager", e);
			}

			return trustManager = tmf;
		}
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	@Override
	protected InetSocketAddress connect(final URI uri, final int port) throws Exception {
		InetSocketAddress address = super.connect(uri, port);

		final SslContext ssl;
		if (uri.getScheme().equalsIgnoreCase("wss")) {
			ssl = SslContext.newClientContext(getTrustManager());
		} else {
			ssl = null;
		}

		HttpHeaders headers = new DefaultHttpHeaders();
		for (Map.Entry<String, String> header : this.headers.entrySet()) {
			headers.add(header.getKey(), header.getValue());
		}

		clientListener = new ClientListener(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, headers));

		Bootstrap b = new Bootstrap();
		b.group(SocketPoller.group())
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					if (ssl != null) p.addLast(ssl.newHandler(ch.alloc(), uri.getHost(), port));
					p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), clientListener);
				}
			});

		channelFuture = b.connect(address);

		return address;
	}

	@Override
	protected boolean checkConnected() throws LuaException, InterruptedException {
		if (!super.checkConnected()) return false;

		if (!channelFuture.isDone()) return false;
		if (!channelFuture.isSuccess()) {
			throw LuaHelpers.rewriteException(channelFuture.cause(), "Cannot open socket");
		}

		if (!channelFuture.channel().isOpen()) return false;

		ChannelFuture handshakeFuture = clientListener.handshakeFuture();
		if (handshakeFuture == null || !handshakeFuture.isDone()) return false;
		if (!handshakeFuture.isSuccess()) {
			throw LuaHelpers.rewriteException(handshakeFuture.cause(), "Cannot open socket");
		}

		return true;
	}

	@Override
	public void close(boolean remove) {
		super.close(remove);
		if (clientListener != null) {
			channelFuture.channel().close();

			clientListener = null;
			channelFuture = null;
		}
	}

	@Override
	protected int write(byte[] contents) throws LuaException, InterruptedException {
		if (checkConnected()) {
			channelFuture.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.wrappedBuffer(contents)));
			return contents.length;
		} else {
			return 0;
		}
	}

	@Override
	protected byte[] read(int count) throws LuaException, InterruptedException {
		if (checkConnected()) {
			ByteBuf stream;
			synchronized (streams) {
				stream = streams.peek();
			}
			if (stream == null) return null;

			int toRead = Math.min(count, stream.capacity() - stream.readerIndex());
			byte[] result = new byte[toRead];
			stream.readBytes(result);

			if (stream.readerIndex() >= stream.capacity()) {
				synchronized (streams) {
					streams.remove();
				}
			}
			return result;
		} else {
			return new byte[0];
		}
	}


	private class ClientListener extends SimpleChannelInboundHandler<Object> {
		private final WebSocketClientHandshaker handshaker;
		private ChannelPromise handshakeFuture;

		public ClientListener(WebSocketClientHandshaker handshaker) {
			this.handshaker = handshaker;
		}

		public ChannelFuture handshakeFuture() {
			return handshakeFuture;
		}

		@Override
		public void handlerAdded(ChannelHandlerContext ctx) {
			handshakeFuture = ctx.newPromise();
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) {
			handshaker.handshake(ctx.channel());
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) {
			WebSocketConnection.this.onClosed();
		}

		@Override
		public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
			Channel ch = ctx.channel();
			if (!handshaker.isHandshakeComplete()) {
				handshaker.finishHandshake(ch, (FullHttpResponse) msg);
				handshakeFuture.setSuccess();

				WebSocketConnection.this.onConnectFinished();
				return;
			}

			if (msg instanceof FullHttpResponse) {
				FullHttpResponse response = (FullHttpResponse) msg;
				throw new IllegalStateException(
					"Unexpected FullHttpResponse (getStatus=" + response.getStatus() +
						", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
			}

			WebSocketFrame frame = (WebSocketFrame) msg;
			if (frame instanceof TextWebSocketFrame) {
				synchronized (streams) {
					WebSocketConnection.this.streams.add(frame.content().copy());
				}
				WebSocketConnection.this.onMessage();
			} else if (frame instanceof BinaryWebSocketFrame) {
				synchronized (streams) {
					WebSocketConnection.this.streams.add(frame.content().copy());
				}
				WebSocketConnection.this.onMessage();
			} else if (frame instanceof CloseWebSocketFrame) {
				ch.close();
				WebSocketConnection.this.onClosed();
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			if (!handshakeFuture.isDone()) {
				handshakeFuture.setFailure(cause);
			}
			ctx.close();

			String message = cause.getMessage();
			WebSocketConnection.this.onError(message == null || message.isEmpty() ? "Unknown socket error" : message);
		}
	}
}
