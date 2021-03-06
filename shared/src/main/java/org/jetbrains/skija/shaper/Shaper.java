package org.jetbrains.skija.shaper;

import java.lang.ref.*;
import java.util.*;
import org.jetbrains.annotations.*;
import org.jetbrains.skija.*;
import org.jetbrains.skija.impl.*;

/**
 * Shapes text using HarfBuzz and places the shaped text into a
 * client-managed buffer.
 */
public class Shaper extends Managed {
    static { Library.staticLoad(); }
    
    @NotNull @Contract("-> new")
    public static Shaper makePrimitive() {
        Stats.onNativeCall();
        return new Shaper(_nMakePrimitive());
    }

    @NotNull @Contract("-> new")
    public static Shaper makeShaperDrivenWrapper() {
        return makeShaperDrivenWrapper(null);
    }

    @NotNull @Contract("_ -> new")
    public static Shaper makeShaperDrivenWrapper(@Nullable FontMgr fontMgr) {
        try {
            Stats.onNativeCall();
            return new Shaper(_nMakeShaperDrivenWrapper(Native.getPtr(fontMgr)));
        } finally {
            Reference.reachabilityFence(fontMgr);
        }
    }

    @NotNull @Contract("-> new")
    public static Shaper makeShapeThenWrap() {
        return makeShapeThenWrap(null);
    }

    @NotNull @Contract("_ -> new")
    public static Shaper makeShapeThenWrap(@Nullable FontMgr fontMgr) {
        try {
            Stats.onNativeCall();
            return new Shaper(_nMakeShapeThenWrap(Native.getPtr(fontMgr)));
        } finally {
            Reference.reachabilityFence(fontMgr);
        }
    }

    @NotNull @Contract("-> new")
    public static Shaper makeShapeDontWrapOrReorder() {
        return makeShapeDontWrapOrReorder(null);
    }

    @NotNull @Contract("_ -> new")
    public static Shaper makeShapeDontWrapOrReorder(@Nullable FontMgr fontMgr) {
        try {
            Stats.onNativeCall();
            return new Shaper(_nMakeShapeDontWrapOrReorder(Native.getPtr(fontMgr)));
        } finally {
            Reference.reachabilityFence(fontMgr);
        }
    }

    /**
     * <p>Only works on macOS</p>
     * 
     * <p>WARN broken in m87 https://bugs.chromium.org/p/skia/issues/detail?id=10897</p>
     * 
     * @return  Shaper on macOS, throws UnsupportedOperationException elsewhere
     */
    @NotNull @Contract("-> new")
    public static Shaper makeCoreText() {
        Stats.onNativeCall();
        long ptr = _nMakeCoreText();
        if (ptr == 0)
            throw new UnsupportedOperationException("CoreText not available");
        return new Shaper(ptr);
    }

    @NotNull @Contract("-> new")
    public static Shaper make() {
        return make(null);
    }

    @NotNull @Contract("_ -> new")
    public static Shaper make(@Nullable FontMgr fontMgr) {
        try {
            Stats.onNativeCall();
            return new Shaper(_nMake(Native.getPtr(fontMgr)));
        } finally {
            Reference.reachabilityFence(fontMgr);
        }
    }

    @Nullable @Contract("_, _ -> new")
    public TextBlob shape(String text, Font font) {
        return shape(text, font, null, true, Float.POSITIVE_INFINITY, Point.ZERO);
    }

    @Nullable @Contract("_, _, _ -> new")
    public TextBlob shape(String text, Font font, float width) {
        return shape(text, font, null, true, width, Point.ZERO);
    }

    @Nullable @Contract("_, _, _, _ -> new")
    public TextBlob shape(String text, Font font, float width, @NotNull Point offset) {
        return shape(text, font, null, true, width, offset);
    }

    @Nullable @Contract("_, _, _, _, _ -> new")
    public TextBlob shape(String text, Font font, boolean leftToRight, float width, @NotNull Point offset) {
        return shape(text, font, null, leftToRight, width, offset);
    }

    @Nullable @Contract("_, _, _, _, _, _ -> new")
    public TextBlob shape(String text, Font font, @Nullable FontFeature[] features, boolean leftToRight, float width, @NotNull Point offset) {
        try {
            Stats.onNativeCall();
            long ptr = _nShapeToTextBlob(_ptr, text, Native.getPtr(font), features, leftToRight, width, offset._x, offset._y);
            return 0 == ptr ? null : new TextBlob(ptr);
        } finally {
            Reference.reachabilityFence(this);
            Reference.reachabilityFence(font);
        }
    }

    @NotNull @Contract("_, _, _, _, _, _, _ -> this")
    public Shaper shape(String text,
                        Font font,
                        @Nullable FontMgr fontMgr,
                        @Nullable FontFeature[] features,
                        boolean leftToRight,
                        float width,
                        RunHandler runHandler)
    {
        try (var textUtf8 = new ManagedString(text);
             var fontIter = new FontMgrRunIterator(textUtf8, false, font, fontMgr);
             var bidiIter = new IcuBidiRunIterator(textUtf8, false, leftToRight ? java.text.Bidi.DIRECTION_LEFT_TO_RIGHT : java.text.Bidi.DIRECTION_RIGHT_TO_LEFT);
             var scriptIter = new HbIcuScriptRunIterator(textUtf8, false);)
        {
            var langIter = new TrivialLanguageRunIterator(text, Locale.getDefault().toLanguageTag());
            return shape(textUtf8, fontIter, bidiIter, scriptIter, langIter, features, width, runHandler);
        }
    }

    @NotNull @Contract("_, _, _, _, _, _, _ -> this")
    public Shaper shape(@NotNull String text,
                        @NotNull Iterator<FontRun> fontIter,
                        @NotNull Iterator<BidiRun> bidiIter,
                        @NotNull Iterator<ScriptRun> scriptIter,
                        @NotNull Iterator<LanguageRun> langIter,
                        @Nullable FontFeature[] features,
                        float width,
                        @NotNull RunHandler runHandler)
    {
        try (ManagedString textUtf8 = new ManagedString(text);) {
            return shape(textUtf8, fontIter, bidiIter, scriptIter, langIter, features, width, runHandler);
        }
    }

    @NotNull @Contract("_, _, _, _, _, _, _ -> this")
    public Shaper shape(@NotNull ManagedString textUtf8,
                        @NotNull Iterator<FontRun> fontIter,
                        @NotNull Iterator<BidiRun> bidiIter,
                        @NotNull Iterator<ScriptRun> scriptIter,
                        @NotNull Iterator<LanguageRun> langIter,
                        @Nullable FontFeature[] features,
                        float width,
                        @NotNull RunHandler runHandler)
    {
        assert fontIter != null : "FontRunIterator == null";
        assert bidiIter != null : "BidiRunIterator == null";
        assert scriptIter != null : "ScriptRunIterator == null";
        assert langIter != null : "LanguageRunIterator == null";
        Stats.onNativeCall();
        _nShape(_ptr, Native.getPtr(textUtf8), fontIter, bidiIter, scriptIter, langIter, features, width, runHandler);
        return this;
    }

    @ApiStatus.Internal
    public Shaper(long ptr) {
        super(ptr, _FinalizerHolder.PTR);
    }

    @ApiStatus.Internal
    public static class _FinalizerHolder {
        public static final long PTR = _nGetFinalizer();
    }

    public static native long _nGetFinalizer();
    public static native long _nMakePrimitive();
    public static native long _nMakeShaperDrivenWrapper(long fontMgrPtr);
    public static native long _nMakeShapeThenWrap(long fontMgrPtr);
    public static native long _nMakeShapeDontWrapOrReorder(long fontMgrPtr);
    public static native long _nMakeCoreText();
    public static native long _nMake(long fontMgrPtr);
    public static native long _nShapeToTextBlob(long ptr, String text, long fontPtr, FontFeature[] features, boolean leftToRight, float width, float offsetX, float offsetY);
    public static native void _nShape(long ptr, long textPtr, Iterator<FontRun> fontIter, Iterator<BidiRun> bidiIter, Iterator<ScriptRun> scriptIter, Iterator<LanguageRun> langIter,
                                      FontFeature[] features, float width, RunHandler runHandler);
}
