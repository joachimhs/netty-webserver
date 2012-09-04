package no.haagensoftware.netty.webserver.handler;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.Charset;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class SimpleResponseHandler extends SimpleChannelUpstreamHandler {
    private String text;
    private HttpResponseStatus status = HttpResponseStatus.OK;

    public SimpleResponseHandler(String text) {
        this.text = text;
    }

    public SimpleResponseHandler(String text, HttpResponseStatus status) {
        this(text);
        this.status = status;
    }

    public SimpleResponseHandler(String text, int status) {
        this(text);
        this.status = HttpResponseStatus.valueOf(status);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	HttpRequest request = (HttpRequest) e.getMessage();
    	String uri = request.getUri();
        System.out.println("uri: " + uri);
        
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(text, Charset.forName("UTF-8")));
        e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
