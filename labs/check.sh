#!/usr/bin/env bash
# 사용법: ./check.sh 02      (labs 폴더에서, 주차 번호)
cd "$(dirname "$0")" || exit 1
w="week$1"
[ -d "$w" ] || { echo "없는 주차입니다: $w"; exit 1; }
out=$(mktemp -d)
javac -encoding UTF-8 -d "$out" lib/T.java "$w"/*.java || {
  echo; echo "❌ 컴파일 에러입니다. 위 메시지의 첫 번째 에러부터 읽고 고치세요."; exit 1; }
echo "== $w 채점 =="
java -Dfile.encoding=UTF-8 -cp "$out" Check "$w"
