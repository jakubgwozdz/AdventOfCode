#!/usr/bin/env sh

DAY=$1

if [ -z "$DAY" ]
then
  DAY=$(date +%-d)
fi

PADDAY=$DAY

if [ "$PADDAY" -lt 10 ]
then
  PADDAY="0$PADDAY"
fi

YEAR=$(date +%Y)
COOKIE=$(cat .cookie)

curl -H "Cookie: session=$COOKIE" "https://adventofcode.com/$YEAR/day/$DAY/input" -o "data/input-$YEAR-$PADDAY.txt"
curl -H "Cookie: session=$COOKIE" "https://adventofcode.com/$YEAR/day/$DAY" -o "data/puzzle-$YEAR-$PADDAY.html"

PKGDIR=advent$YEAR/day$PADDAY
FILE=src/$PKGDIR/Day$PADDAY.kt
TESTFILE=testSrc/$PKGDIR/Day${PADDAY}KtTest.kt

mkdir -p "src/${PKGDIR}"
mkdir -p "testSrc/${PKGDIR}"

if [ ! -f "$FILE" ]
then
  echo "package advent$YEAR.day$PADDAY" >> "$FILE"
fi

if [ ! -f "$TESTFILE" ]
then
  echo "package advent$YEAR.day$PADDAY" >> "$TESTFILE"
  echo "internal class Day${PADDAY}KtTest { }" >> "$TESTFILE"
fi
