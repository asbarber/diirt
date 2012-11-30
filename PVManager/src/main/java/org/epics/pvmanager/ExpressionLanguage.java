/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager;

import org.epics.pvmanager.expression.ChannelExpressionList;
import org.epics.pvmanager.expression.ChannelExpression;
import org.epics.pvmanager.expression.DesiredRateReadWriteExpression;
import org.epics.pvmanager.expression.DesiredRateExpressionList;
import org.epics.pvmanager.expression.DesiredRateExpressionImpl;
import org.epics.pvmanager.expression.WriteExpressionImpl;
import org.epics.pvmanager.expression.DesiredRateExpression;
import org.epics.pvmanager.expression.WriteExpression;
import org.epics.pvmanager.expression.SourceRateExpression;
import org.epics.pvmanager.expression.SourceRateReadWriteExpression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.epics.pvmanager.expression.Cache;
import org.epics.pvmanager.expression.DesiredRateExpressionListImpl;
import org.epics.pvmanager.expression.DesiredRateReadWriteExpressionImpl;
import org.epics.pvmanager.expression.DesiredRateReadWriteExpressionList;
import org.epics.pvmanager.expression.DesiredRateReadWriteExpressionListImpl;
import org.epics.pvmanager.expression.ReadMap;
import org.epics.pvmanager.expression.Queue;
import org.epics.pvmanager.expression.ReadWriteMap;
import org.epics.pvmanager.expression.SourceRateExpressionList;
import org.epics.pvmanager.expression.SourceRateReadWriteExpressionList;
import org.epics.pvmanager.expression.WriteExpressionList;
import org.epics.pvmanager.expression.WriteMap;
import org.epics.util.time.TimeDuration;

/**
 * Operators to constructs expression of PVs that the {@link PVManager} will
 * be able to monitor.
 *
 * @author carcassi
 */
public class ExpressionLanguage {

    static {
        // Install support for basic java types
        BasicTypeSupport.install();
    }
    
    private ExpressionLanguage() {}
    
    /**
     * Creates a constant expression that always return that object.
     * This is useful to test expressions or to introduce data that is available
     * at connection time at that will not change.
     * 
     * @param <T> type of the value
     * @param value the actual value
     * @return an expression that is always going to return the given value
     */
    public static <T> DesiredRateExpression<T> constant(T value) {
        return constant(value, value.toString());
    }
    
    /**
     * Creates a constant expression that always return that object, with the
     * given name for the expression.
     * This is useful to test expressions or to introduce data that is available
     * at connection time at that will not change.
     * 
     * @param <T> type of the value
     * @param value the actual value
     * @param name the name of the expression
     * @return an expression that is always going to return the given value
     */
    public static <T> DesiredRateExpression<T> constant(T value, String name) {
        Class<?> clazz = Object.class;
        if (value != null)
            clazz = value.getClass();
        @SuppressWarnings("unchecked")
        ValueCache<T> cache = (ValueCache<T>) new ValueCacheImpl(clazz);
        if (value != null)
            cache.writeValue(value);
        return new DesiredRateExpressionImpl<T>(new DesiredRateExpressionListImpl<T>(), cache, name);
    }

    /**
     * A channel with the given name of any type. This expression can be
     * used both in a read and a write expression.
     *
     * @param name the channel name
     * @return an expression representing the channel
     */
    public static ChannelExpression<Object, Object> channel(String name) {
        return channel(name, Object.class, Object.class);
    }

    /**
     * A channel with the given name and type. This expression can be
     * used both in a read and a write expression.
     *
     * @param <R> read payload
     * @param <W> write payload
     * @param name the channel name
     * @param readType type being read
     * @param writeType type being written
     * @return an expression representing the channel
     */
    public static <R, W> ChannelExpression<R, W> channel(String name, Class<R> readType, Class<W> writeType) {
        if (name == null) {
            return new ChannelExpression<R, W>(readType, writeType);
        } else {
            return new ChannelExpression<R, W>(name, readType, writeType);
        }
    }

    /**
     * A list of channels with the given names of any type. This expression can be
     * used both in a read and a write expression.
     *
     * @param names the channel names; can't be null
     * @return an list of expressions representing the channels
     */
    public static ChannelExpressionList<Object, Object> channels(String... names) {
        return channels(Arrays.asList(names), Object.class, Object.class);
    }

    /**
     * A list of channels with the given names and type. This expression can be
     * used both in a read and a write expression.
     *
     * @param <R> read payload
     * @param <W> write payload
     * @param readType type being read
     * @param writeType type being written
     * @param names the channel names; can't be null
     * @return an list of expressions representing the channels
     */
    public static <R, W> ChannelExpressionList<R, W> channels(Collection<String> names, Class<R> readType, Class<W> writeType) {
        return new ChannelExpressionList<R, W>(names, readType, writeType);
    }

    /**
     * A list of channels with the given names of any type. This expression can be
     * used both in a read and a write expression.
     *
     * @param names the channel names; can't be null
     * @return an list of expressions representing the channels
     */
    public static ChannelExpressionList<Object, Object> channels(Collection<String> names) {
        return channels(names, Object.class, Object.class);
    }

    /**
     * Returns all the new values generated by the expression source rate.
     *
     * @param <T> type being read
     * @param expressions source rate expressions
     * @return a new expression
     */
    public static <T> DesiredRateExpressionList<List<T>>
            newValuesOf(SourceRateExpressionList<T> expressions) {
        DesiredRateExpressionList<List<T>> list = new DesiredRateExpressionListImpl<List<T>>();
        for (SourceRateExpression<T> expression : expressions.getSourceRateExpressions()) {
            list.and(newValuesOf(expression));
        }
        return list;
    }

    /**
     * Returns all the new values generated by the expression source rate.
     *
     * @param <T> type being read
     * @param expression source rate expression
     * @return a new expression
     */
    public static <T> DesiredRateExpression<List<T>>
            newValuesOf(SourceRateExpression<T> expression) {
        return new DesiredRateExpressionImpl<List<T>>(expression,
                new QueueCollector<T>(10),
                expression.getName());
    }

    /**
     * Returns up to maxValues new values generated by the expression source rate.
     *
     * @param <T> type being read
     * @param expression source rate expression
     * @param maxValues maximum number of values to send with each notification
     * @return a new expression
     */
    public static <T> DesiredRateExpression<List<T>>
            newValuesOf(SourceRateExpression<T> expression, int maxValues) {
        return new DesiredRateExpressionImpl<List<T>>(expression,
                new QueueCollector<T>(maxValues),
                expression.getName());
    }
    
    /**
     * Expression that returns (only) the latest value computed
     * from a {@code SourceRateExpression}.
     *
     * @param <T> type being read
     * @param expression expression read at the source rate
     * @return a new expression
     */
    public static <T> DesiredRateExpression<T> latestValueOf(SourceRateExpression<T> expression) {
        return new DesiredRateExpressionImpl<T>(expression,
                new LatestValueCollector<T>(),
                expression.getName());
    }

    /**
     * Expression that returns (only) the latest value computed
     * from a {@code SourceRateExpression}.
     *
     * @param <T> type being read
     * @param expressions expressions read at the source rate
     * @return an expression list
     */
    public static <T> DesiredRateExpressionList<T> latestValueOf(SourceRateExpressionList<T> expressions) {
        DesiredRateExpressionList<T> list = new DesiredRateExpressionListImpl<T>();
        for (SourceRateExpression<T> expression : expressions.getSourceRateExpressions()) {
            list.and(latestValueOf(expression));
        }
        return list;
    }

    /**
     * For reads, returns (only) the latest value computed
     * from a {@code SourceRateReadWriteExpression}; for writes, same
     * as the given expression.
     *
     * @param <R> read payload
     * @param <W> write payload
     * @param expression expression read at the source rate
     * @return a new expression
     */
    public static <R, W> DesiredRateReadWriteExpression<R, W> latestValueOf(SourceRateReadWriteExpression<R, W> expression) {
        return new DesiredRateReadWriteExpressionImpl<R, W>(latestValueOf((SourceRateExpression<R>) expression), expression);
    }

    /**
     * For reads, returns (only) the latest value computed
     * from a {@code SourceRateReadWriteExpression}; for writes, same
     * as the given expression.
     *
     * @param <R> read payload
     * @param <W> write payload
     * @param expressions expressions read at the source rate
     * @return a new expression
     */
    public static <R, W> DesiredRateReadWriteExpressionList<R, W> latestValueOf(SourceRateReadWriteExpressionList<R, W> expressions) {
        DesiredRateReadWriteExpressionListImpl<R, W> list = new DesiredRateReadWriteExpressionListImpl<R, W>();
        for (SourceRateReadWriteExpression<R, W> expression : expressions.getSourceRateReadWriteExpressions()) {
            list.and(latestValueOf(expression));
        }
        return list;
    }
    
    /**
     * A user provided single argument function.
     *
     * @param <R> result type
     * @param <A> argument type
     */
    public static interface OneArgFunction<R, A> {
        /**
         * Calculates the new value.
         *
         * @param arg argument
         * @return result
         */
        R calculate(A arg);
    }

    /**
     * A user provided double argument function.
     *
     * @param <R> result type
     * @param <A1> first argument type
     * @param <A2> second argument type
     */
    public static interface TwoArgFunction<R, A1, A2> {
        /**
         * Calculates the new value.
         *
         * @param arg1 first argument
         * @param arg2 second argument
         * @return result
         */
        R calculate(A1 arg1, A2 arg2);
    }

    /**
     * An expression that represents the result of a user provided function.
     *
     * @param <R> result type
     * @param <A> argument type
     * @param function the user provided function
     * @param argExpression expression for the function argument
     * @return a new expression
     */
    public static <R, A> DesiredRateExpression<R> resultOf(final OneArgFunction<R, A> function,
            DesiredRateExpression<A> argExpression) {
        String name = function.getClass().getSimpleName() + "(" + argExpression.getName() + ")";
        final ReadFunction<A> arg = argExpression.getFunction();
        return new DesiredRateExpressionImpl<R>(argExpression, new ReadFunction<R>() {
            @Override
            public R readValue() {
                return function.calculate(arg.readValue());
            }
        }, name);
    }

    /**
     * An expression that represents the result of a user provided function.
     *
     * @param <R> result type
     * @param <A1> first argument type
     * @param <A2> second argument type
     * @param function the user provided function
     * @param arg1Expression expression for the first argument
     * @param arg2Expression expression for the second argument
     * @return a new expression
     */
    public static <R, A1, A2> DesiredRateExpression<R> resultOf(final TwoArgFunction<R, A1, A2> function,
            DesiredRateExpression<? extends A1> arg1Expression, DesiredRateExpression<? extends A2> arg2Expression) {
        return resultOf(function, arg1Expression, arg2Expression, function.getClass().getSimpleName() + "(" + arg1Expression.getName() +
                ", " + arg2Expression.getName() + ")");
    }

    /**
     * An expression that represents the result of a user provided function.
     *
     * @param <R> result type
     * @param <A1> first argument type
     * @param <A2> second argument type
     * @param function the user provided function
     * @param arg1Expression expression for the first argument
     * @param arg2Expression expression for the second argument
     * @param name expression name
     * @return a new expression
     */
    public static <R, A1, A2> DesiredRateExpression<R> resultOf(final TwoArgFunction<R, A1, A2> function,
            DesiredRateExpression<? extends A1> arg1Expression, DesiredRateExpression<? extends A2> arg2Expression, String name) {
        final ReadFunction<? extends A1> arg1 = arg1Expression.getFunction();
        final ReadFunction<? extends A2> arg2 = arg2Expression.getFunction();
        @SuppressWarnings("unchecked")
        DesiredRateExpressionList<? extends Object> argExpressions =
                new DesiredRateExpressionListImpl<Object>().and(arg1Expression).and(arg2Expression);
        return new DesiredRateExpressionImpl<R>(argExpressions,
                new ReadFunction<R>() {
                    @Override
                    public R readValue() {
                        return function.calculate(arg1.readValue(), arg2.readValue());
                    }
                }, name);
    }

    /**
     * Filters a data stream, removing updates that match the given function.
     * Looks for objects of a specific type,
     * and filters based on previous and current value.
     * 
     * @param <T> the type to cast to before the filtering
     */
    public static abstract class Filter<T> {

        private final Class<T> clazz;
        private final boolean filterUnmatched;

        Filter() {
            clazz = null;
            filterUnmatched = false;
        }

        /**
         * Creates a filter which looks for and cases data objects of the
         * given class.
         *
         * @param clazz the argument type of the filter
         */
        public Filter(Class<T> clazz) {
            this(clazz, false);
        }

        /**
         * Creates a filter which looks for and cases data objects of the
         * given class. If objects do not match, returns filterUnmatched.
         *
         * @param clazz the argument type of the filter
         * @param filterUnmatched whether objects that don't match the class
         * should be filtered or not
         */
        public Filter(Class<T> clazz, boolean filterUnmatched) {
            this.clazz = clazz;
            this.filterUnmatched = filterUnmatched;
        }

        // This is what the framework should actually call: it does the
        // type checking and casting
        boolean innerFilter(Object previousValue, Object currentValue) {
            if ((previousValue == null || clazz.isInstance(previousValue)) &&
                    (currentValue == null || clazz.isInstance(currentValue))) {
                return filter(clazz.cast(previousValue), clazz.cast(currentValue));
            }
            return filterUnmatched;
        }

        /**
         * Determines whether the new value should be filtered or not. The
         * filtering is done based on the previousValue, which is always a
         * value that passed the filtering. The first value ever to be
         * passed to the filter will have null for previousValue.
         *
         * @param previousValue the previous data update
         * @param currentValue the current data update
         * @return true if the current data update should be dropped
         */
        public abstract boolean filter(T previousValue, T currentValue);

        /**
         * Returns a new filter that is the logical AND of this and the given
         * one.
         *
         * @param filter another filter
         * @return a new filter that is the AND of the two
         */
        public Filter<?> and(final Filter<?> filter) {
            return new Filter<Object>() {

                @Override
                public boolean innerFilter(Object previousValue, Object currentValue) {
                    return super.innerFilter(previousValue, currentValue) &&
                            filter.innerFilter(previousValue, currentValue);
                }

                @Override
                public boolean filter(Object previousValue, Object currentValue) {
                    throw new UnsupportedOperationException("Not used.");
                }

            };
        }

        /**
         * Returns a new filter that is the logical OR of this and the given
         * one.
         *
         * @param filter another filter
         * @return a new filter that is the OR of the two
         */
        public Filter<?> or(final Filter<?> filter) {
            return new Filter<Object>() {

                @Override
                public boolean innerFilter(Object previousValue, Object currentValue) {
                    return super.innerFilter(previousValue, currentValue) ||
                            filter.innerFilter(previousValue, currentValue);
                }

                @Override
                public boolean filter(Object previousValue, Object currentValue) {
                    throw new UnsupportedOperationException("Not used.");
                }

            };
        }
    }

    /**
     * Filters a stream of updates with the given filter.
     *
     * @param <T> the type of data streaming in and out
     * @param filter the filtering function
     * @param expression the argument expression
     * @return a new expression for the filtering result
     */
    public static <T> DesiredRateExpression<List<T>> filterBy(final Filter<?> filter,
            DesiredRateExpression<List<T>> expression) {
        String name = expression.getName();
        final ReadFunction<List<T>> arg = expression.getFunction();
        return new DesiredRateExpressionImpl<List<T>>(expression,
                new ReadFunction<List<T>>() {

                    private T previousValue;

                    @Override
                    public List<T> readValue() {
                        List<T> list = arg.readValue();
                        List<T> newList = new ArrayList<T>();
                        for (T element : list) {
                            if (!filter.innerFilter(previousValue, element)) {
                                newList.add(element);
                                previousValue = element;
                            }
                        }
                        return newList;
                    }
                }, name);
    }
    
    // Static collections (no change after expression creation

    /**
     * Converts a list of expressions to an expression that returns the list of results.
     * 
     * @param <T> type being read
     * @param expressions a list of expressions
     * @return an expression representing the list of results
     */
    public static <T> DesiredRateExpression<List<T>> listOf(DesiredRateExpressionList<T> expressions) {
        // Calculate all the needed functions to combine
        List<ReadFunction> functions = new ArrayList<ReadFunction>();
        for (DesiredRateExpression<T> expression : expressions.getDesiredRateExpressions()) {
            functions.add(expression.getFunction());
        }

        @SuppressWarnings("unchecked")
        DesiredRateExpression<List<T>> expression = new DesiredRateExpressionImpl<List<T>>(expressions,
                (ReadFunction<List<T>>) (ReadFunction) new ListOfFunction(functions), null);
        return expression;
    }
    
    /**
     * An empty map that can manage expressions of the given type.
     * <p>
     * The returned expression is dynamic, which means child expressions
     * can be added or removed from the map.
     * 
     * @param <R> the type of the values
     * @param clazz the type of the values
     * @return an expression representing a map from name to results
     */
    public static <R> ReadMap<R> readMapOf(Class<R> clazz){
        return new ReadMap<>();
    }
    
    /**
     * An empty map that can write expressions of the given type.
     * <p>
     * The returned expression is dynamic, which means child expressions
     * can be added or removed from the map.
     * 
     * @param <W> the type of the values
     * @param clazz the type of the values
     * @return an expression representing a map from name to results
     */
    public static <W> WriteMap<W> writeMapOf(Class<W> clazz){
        return new WriteMap<>();
    }
    
    /**
     * An empty map that can read/write expressions of the given type.
     * <p>
     * The returned expression is dynamic, which means child expressions
     * can be added or removed from the map.
     * 
     * @param <R> the type of the values to read
     * @param <W> the type of the values to write
     * @param readClass the type of the values to read
     * @param writeClass the type of the values to write
     * @return an expression representing a map from name to results
     */
    public static <R, W> ReadWriteMap<R, W> mapOf(Class<R> readClass, Class<W> writeClass){
        return new ReadWriteMap<>();
    }
    
    /**
     * An expression that returns a key/value map where the key is the
     * expression name and the value is the expression value.
     * <p>
     * The returned expression is dynamic, which means child expressions
     * can be added or removed from the map.
     * 
     * @param <R> the type of the values
     * @param expressions a list of expressions
     * @return an expression representing a map from name to results
     */
    public static <R> ReadMap<R> mapOf(DesiredRateExpressionList<R> expressions){
        return new ReadMap<R>().add(expressions);
    }
    
    /**
     * An expression that expects a key/value map where the key is the
     * expression name and the value is the expression value.
     * <p>
     * The returned expression is dynamic, which means child expressions
     * can be added or removed from the map.
     * 
     * @param <W> the type of the values
     * @param expressions a list of expressions
     * @return an expression representing a map from name to results
     */
    public static <W> WriteMap<W> mapOf(WriteExpressionList<W> expressions){
        return new WriteMap<W>().add(expressions);
    }
    
    /**
     * An expression that works on a key/value map where the key is the
     * expression name and the value is the expression value.
     * <p>
     * The returned expression is dynamic, which means child expressions
     * can be added or removed from the map.
     * 
     * @param <R> the type for the read values
     * @param <W> the type for the write values
     * @param expressions a list of expressions
     * @return an expression representing a map from name to results
     */
    public static <R, W> ReadWriteMap<R, W> mapOf(DesiredRateReadWriteExpressionList<R, W> expressions){
        return new ReadWriteMap<R, W>().add(expressions);
    }
    
    /**
     * A queue of objects of the given class. By default, it holds at maximum
     * 10 elements.
     * <p>
     * This can be used to create expressions where the source of the data is
     * not just pvmanager data sources. One can add new values to a queue
     * from any thread, and in response to any event, such as user input,
     * updates from time consuming tasks or responses from services.
     * 
     * @param <R> the type to be kept in the queue
     * @param clazz the type for the values to be kept in the queue
     * @return a new queue
     */
    public static <R> Queue<R> queueOf(Class<R> clazz) {
        return new Queue<>(10);
    }
    
    /**
     * A cache of objects of the given class. By default, it holds at maximum
     * 10 elements.
     * <p>
     * This can be used to create expressions where the source of the data is
     * not just pvmanager data sources. One can add new values to the cache
     * from any thread, and in response to any event, such as user input,
     * updates from time consuming tasks or responses from services.
     * 
     * @param <R> the type to be kept in the queue
     * @param clazz the type for the values to be kept in the queue
     * @return a new queue
     */
    public static <R> Cache<R> cacheOf(Class<R> clazz) {
        return new Cache<>(10);
    }
}
