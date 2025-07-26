#!/bin/bash

echo "🔍 Debugging Category Names"
echo "============================"

echo "1. Searching for category names in MainActivity files..."
echo "   Original MainActivity categories:"
grep -A 10 "nav_live" app/scr/main/java/com/cinecraze/free/stream/MainActivityOld.java | grep "filterEntries"

echo "   New MainActivity categories:"
grep -A 10 "nav_live" app/scr/main/java/com/cinecraze/free/stream/MainActivity.java | grep "category ="

echo ""
echo "2. Common category name variations found in code:"
echo "   - Movies"
echo "   - TV Series"
echo "   - TV Shows" 
echo "   - Live TV"
echo "   - Live"

echo ""
echo "3. To fix the issue, we need to:"
echo "   a) Use the correct category names that match the API data"
echo "   b) Add logging to see what categories are actually stored"
echo "   c) Update the navigation to use the correct names"

echo ""
echo "💡 Suggested fixes:"
echo "   - Change 'Live' to 'Live TV'"
echo "   - Change 'TV Shows' to 'TV Series' (or vice versa)"
echo "   - Add logging to debug actual category names"