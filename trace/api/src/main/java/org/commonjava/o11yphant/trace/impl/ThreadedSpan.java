package org.commonjava.o11yphant.trace.impl;

import org.commonjava.o11yphant.trace.spi.adapter.SpanAdapter;
import org.commonjava.o11yphant.trace.thread.ThreadedTraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class ThreadedSpan
                implements SpanAdapter
{
    private final SpanAdapter span;

    private final Optional<SpanAdapter> parentSpan;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public ThreadedSpan( SpanAdapter span, Optional<SpanAdapter> parentSpan )
    {
        this.span = span;
        this.parentSpan = parentSpan;
    }

    @Override
    public boolean isLocalRoot()
    {
        return span.isLocalRoot();
    }

    @Override
    public String getTraceId()
    {
        return span.getTraceId();
    }

    @Override
    public String getSpanId()
    {
        return span.getSpanId();
    }

    @Override
    public void addField( String name, Object value )
    {
        span.addField( name, value );
    }

    @Override
    public Map<String, Object> getFields()
    {
        return span.getFields();
    }

    @Override
    public void close()
    {
        parentSpan.ifPresent( parent->{
            Map<String, Object> localFields = span.getFields();
            try
            {
                parent.getInProgressFields().forEach( (key,parentVal) -> {
                    Object localVal = localFields.get( key );
                    if ( localVal == null )
                    {
                        span.setInProgressField( key, parentVal );
                    }
                    else if ( parentVal instanceof Long )
                    {
                        span.setInProgressField( key, ( (Long) localVal + (Long) parentVal ) );
                    }
                    else if ( parentVal instanceof Integer )
                    {
                        span.setInProgressField( key, ( (Integer) localVal + (Integer) parentVal ) );
                    }
                    else if ( parentVal instanceof Double )
                    {
                        span.setInProgressField( key, ( (Double) localVal + (Double) parentVal ) );
                    }
                });
            }
            catch ( Throwable t )
            {
                logger.error( "Failed to propagate cumulative trace metrics back from child to parent spans: "
                                              + t.getLocalizedMessage(), t );
            }
        } );
        span.close();
    }

    @Override
    public void setInProgressField( String key, Object value )
    {
        span.setInProgressField( key, value );
    }

    @Override
    public <V> V getInProgressField( String key, V defValue )
    {
        return span.getInProgressField( key, defValue );
    }

    @Override
    public void clearInProgressField( String key )
    {
        span.clearInProgressField( key );
    }

    @Override
    public Map<String, Object> getInProgressFields()
    {
        return span.getInProgressFields();
    }
}
