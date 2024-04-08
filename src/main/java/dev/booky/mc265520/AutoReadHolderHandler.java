/*
 * MIT License
 *
 * Copyright (c) 2018 Velocity team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.booky.mc265520;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A variation on {@link io.netty.handler.flow.FlowControlHandler} that explicitly holds messages
 * on {@code channelRead} and only releases them on an explicit read operation.
 *
 * @author Andrew Steinborn &lt;git@steinborn.me&gt;
 * @see <a href="https://github.com/PaperMC/Velocity/blob/7ca40094cb208abdc29beaad3dcafeb9b3488618/proxy/src/main/java/com/velocitypowered/proxy/network/pipeline/AutoReadHolderHandler.java">https://github.com/PaperMC/Velocity/blob/7ca40094cb208abdc29beaad3dcafeb9b3488618/proxy/src/main/java/com/velocitypowered/proxy/network/pipeline/AutoReadHolderHandler.java</a> (MIT license)
 */
public class AutoReadHolderHandler extends ChannelDuplexHandler {

    private final Queue<Object> queuedMessages;

    public AutoReadHolderHandler() {
        this.queuedMessages = new ArrayDeque<>();
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        drainQueuedMessages(ctx);
        ctx.read();
    }

    private void drainQueuedMessages(ChannelHandlerContext ctx) {
        if (!this.queuedMessages.isEmpty()) {
            Object queued;
            while ((queued = this.queuedMessages.poll()) != null) {
                ctx.fireChannelRead(queued);
            }
            ctx.fireChannelReadComplete();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx.channel().config().isAutoRead()) {
            ctx.fireChannelRead(msg);
        } else {
            this.queuedMessages.add(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (this.queuedMessages.isEmpty()) {
            ctx.fireChannelReadComplete();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        for (Object message : this.queuedMessages) {
            ReferenceCountUtil.release(message);
        }
        this.queuedMessages.clear();
    }
}
