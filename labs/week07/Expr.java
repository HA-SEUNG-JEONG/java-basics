// 수식 트리 (이 파일은 수정하지 마세요). sealed 이므로 종류는 이 세 가지뿐이다.
sealed interface Expr permits Num, Add, Mul {}

record Num(int value) implements Expr {}

record Add(Expr left, Expr right) implements Expr {}

record Mul(Expr left, Expr right) implements Expr {}
