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

curl -H "Cookie: session=$COOKIE" "https://adventofcode.com/$YEAR/day/$DAY/input" -o "input-$YEAR-$PADDAY.txt"
