package org.commonjava.o11yphant.otel.impl;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.ContextStorageProvider;
import io.opentelemetry.context.Scope;
import org.commonjava.cdi.util.weft.ThreadContext;
import org.commonjava.o11yphant.otel.impl.adapter.OtelSpanContext;
import org.commonjava.o11yphant.otel.impl.adapter.OtelType;
import org.commonjava.o11yphant.trace.spi.adapter.SpanAdapter;
import org.commonjava.o11yphant.trace.spi.adapter.SpanContext;
import org.commonjava.o11yphant.trace.thread.ThreadTracingContext;

import java.util.Optional;
import java.util.Properties;

public class OtelThreadTracingContext implements ThreadTracingContext<OtelType>, ContextStorageProvider, ContextStorage
{
    private static final String OTEL_ATTACHED = "opentelemetry-attached-context";

    static
    {
        Properties properties = System.getProperties();
        properties.put("io.opentelemetry.context.contextStorageProvider", OtelThreadTracingContext.class);
    }
    @Override
    public void reinitThreadSpans()
    {
    }

    @Override
    public void clearThreadSpans()
    {
        // FIXME: Not sure this is meaningful at all...
        ThreadContext ctx = ThreadContext.getContext( false );
        if ( ctx != null )
        {
            ctx.remove( OTEL_ATTACHED );
        }
    }

    @Override
    public ContextStorage get()
    {
        return this;
    }

    @Override
    public Scope attach( Context context )
    {
        ThreadContext.getContext( true ).put( OTEL_ATTACHED, context );
        return () -> {
            ThreadContext ctx = ThreadContext.getContext( false );
            if ( ctx != null )
            {
                ctx.remove( OTEL_ATTACHED );
            }
        };
    }

    @Override
    public Context current()
    {
        ThreadContext ctx = ThreadContext.getContext( false );
        if ( ctx != null )
        {
            return (Context) ctx.get( OTEL_ATTACHED );
        }

        return Context.root();
    }
}
