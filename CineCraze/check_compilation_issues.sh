#!/bin/bash

echo "🔍 Comprehensive Compilation Issues Check"
echo "========================================="

# 1. Check for incorrect class names
echo "1. Checking for class name mismatches..."
find app/scr/main/java -name "*.java" | while read file; do
    filename=$(basename "$file" .java)
    classname=$(grep "^public class" "$file" | sed 's/.*class \([A-Za-z0-9_]*\).*/\1/')
    if [ "$filename" != "$classname" ] && [ -n "$classname" ]; then
        echo "   ❌ $file: Class '$classname' should be in file '$classname.java'"
    fi
done

# 2. Check for incorrect .this references
echo "2. Checking for incorrect .this references..."
find app/scr/main/java -name "*.java" | while read file; do
    filename=$(basename "$file" .java)
    incorrect_refs=$(grep -n "MainActivity\.this\|PaginatedMainActivity\.this\|FastPaginatedMainActivity\.this" "$file" | grep -v "$filename\.this")
    if [ -n "$incorrect_refs" ]; then
        echo "   ❌ $file has incorrect .this references:"
        echo "$incorrect_refs" | sed 's/^/      /'
    fi
done

# 3. Check for missing imports
echo "3. Checking for potential missing imports..."
find app/scr/main/java -name "*.java" -exec grep -l "PaginatedMovieAdapter\|MovieAdapter" {} \; | while read file; do
    if ! grep -q "import.*Adapter" "$file" && ! grep -q "class.*Adapter" "$file"; then
        echo "   ⚠️  $file might be missing adapter imports"
    fi
done

# 4. Check for undefined variables/methods
echo "4. Checking for common undefined references..."
grep -r "allEntries" app/scr/main/java --include="*.java" | grep -v "MainActivityOld\|MainActivitySimple" | while read line; do
    file=$(echo "$line" | cut -d: -f1)
    if [ "$(basename "$file")" != "MainActivity.java" ]; then
        echo "   ⚠️  $line"
    fi
done

# 5. Check AndroidManifest
echo "5. Checking AndroidManifest.xml..."
if grep -q 'android:name="\.MainActivity"' app/scr/main/AndroidManifest.xml; then
    echo "   ✅ AndroidManifest.xml correctly references MainActivity"
else
    echo "   ❌ AndroidManifest.xml has incorrect activity reference"
fi

# 6. Check for duplicate class definitions
echo "6. Checking for duplicate class definitions..."
find app/scr/main/java -name "*.java" -exec grep -l "public class MainActivity" {} \; | wc -l | while read count; do
    if [ "$count" -gt 1 ]; then
        echo "   ❌ Multiple MainActivity class definitions found"
        find app/scr/main/java -name "*.java" -exec grep -l "public class MainActivity" {} \;
    else
        echo "   ✅ Single MainActivity class definition found"
    fi
done

echo ""
echo "✅ Compilation check complete!"
echo ""
echo "🎯 Expected structure:"
echo "- MainActivity.java (new pagination) - ACTIVE"
echo "- MainActivityOld.java (backup) - FIXED"
echo "- MovieAdapter.java - COMPATIBLE"
echo "- PaginatedMovieAdapter.java - USED BY NEW MAIN"