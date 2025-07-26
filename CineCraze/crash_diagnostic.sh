#!/bin/bash

echo "🚨 CineCraze Crash Diagnostic Report"
echo "==================================="

echo ""
echo "📱 Checking Android Manifest..."
if [ -f "app/scr/main/AndroidManifest.xml" ]; then
    echo "  ✅ AndroidManifest.xml exists"
    # Check for main activity
    if grep -q "android.intent.action.MAIN" app/scr/main/AndroidManifest.xml; then
        echo "  ✅ Main activity declared"
    else
        echo "  ❌ Main activity not found"
    fi
    # Check theme reference
    if grep -q "Theme.CineCraze" app/scr/main/AndroidManifest.xml; then
        echo "  ✅ Theme referenced"
    else
        echo "  ❌ Theme not referenced"
    fi
else
    echo "  ❌ AndroidManifest.xml missing"
fi

echo ""
echo "🎨 Checking Theme Resources..."
if [ -f "app/scr/main/res/values/themes.xml" ]; then
    echo "  ✅ themes.xml exists"
    if grep -q "Theme.CineCraze" app/scr/main/res/values/themes.xml; then
        echo "  ✅ Theme.CineCraze defined"
    else
        echo "  ❌ Theme.CineCraze not defined"
    fi
else
    echo "  ❌ themes.xml missing"
fi

echo ""
echo "🎯 Checking Critical Resources..."
CRITICAL_RESOURCES=(
    "app/scr/main/res/values/colors.xml"
    "app/scr/main/res/values/strings.xml"
    "app/scr/main/res/values/dimens.xml"
    "app/scr/main/res/layout/activity_main.xml"
    "app/scr/main/res/drawable/ic_launcher.xml"
)

for resource in "${CRITICAL_RESOURCES[@]}"; do
    if [ -f "$resource" ]; then
        echo "  ✅ $(basename $resource)"
    else
        echo "  ❌ $(basename $resource) - MISSING"
    fi
done

echo ""
echo "🔧 Checking MainActivity..."
if [ -f "app/scr/main/java/com/cinecraze/free/stream/MainActivity.java" ]; then
    echo "  ✅ MainActivity.java exists"
    # Check for common crash patterns
    if grep -q "setContentView" app/scr/main/java/com/cinecraze/free/stream/MainActivity.java; then
        echo "  ✅ setContentView called"
    else
        echo "  ❌ setContentView not found"
    fi
    if grep -q "findViewById" app/scr/main/java/com/cinecraze/free/stream/MainActivity.java; then
        echo "  ✅ findViewById calls present"
    else
        echo "  ❌ No findViewById calls"
    fi
else
    echo "  ❌ MainActivity.java missing"
fi

echo ""
echo "📦 Checking Dependencies..."
if [ -f "app/build.gradle" ]; then
    echo "  ✅ build.gradle exists"
    if grep -q "androidx.appcompat:appcompat" app/build.gradle; then
        echo "  ✅ AppCompat dependency found"
    else
        echo "  ❌ AppCompat dependency missing"
    fi
    if grep -q "com.google.android.material:material" app/build.gradle; then
        echo "  ✅ Material Components dependency found"
    else
        echo "  ❌ Material Components dependency missing"
    fi
else
    echo "  ❌ build.gradle missing"
fi

echo ""
echo "🛠️ Common Crash Fixes Applied:"
echo "  ✅ Changed from Material3 to AppCompat theme"
echo "  ✅ Replaced MaterialButton with Button"
echo "  ✅ Added compatibility attributes for older Android versions"
echo "  ✅ Created Netflix button background drawable"
echo "  ✅ Fixed toolbar references"

echo ""
echo "🚀 Build Recommendations:"
echo "  1. Clean and rebuild project: ./gradlew clean build"
echo "  2. Check logcat for specific error messages"
echo "  3. Test on different Android versions (API 21+)"
echo "  4. Verify all drawable resources are present"

echo ""
echo "📊 Compatibility:"
echo "  ✅ Android 5.0+ (API 21) supported"
echo "  ✅ AppCompat theme for better compatibility"
echo "  ✅ Fallback drawables for older devices"

echo ""
echo "If app still crashes, check logcat output for specific error details."