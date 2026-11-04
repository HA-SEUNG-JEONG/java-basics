화면에서 GET /books 를 펼치고 Try it out → Execute. Network 탭에 books 요청이 하나 생깁니다. 클릭해서 다음을 노트에 적으세요.

Headers 탭의 General 에서 Request URL, Request Method, Status Code.
Request URL : http://localhost:8080/books
Request Method : GET
Status Code : 200 OK

Response Headers 의 Content-Type은 "application/json"
curl -i 로 본 헤더(Vary, X-Content-Type-Options, Cache-Control, Content-Type, Transfer-Encoding, Date 등)가 Response Headers 에 같은 이름으로 보임
Response는 [{"id":1,"isbn":"1","title":"t","author":"a"}]

books/999로 GET 요청을 보낼 경우 404 코드가 뜨며, 응답은 {"message":"책을 찾을 수 없습니다: 999"} 로 나옴
브라우저는 4xx 를 실패(빨간색)로 표시하지만, fetch 입장에서는 응답을 받았으므로 성공임. 예외 없이 res.ok 만 false 가 됨 (3단계에서 본 것과 같음)

/auth/login으로 POST 요청을 보낼 경우 {"username":"demo"}로 응답이 내려오며, Response Headers > Set-Cookie 값은 JSESSIONID=B78674FB26AA2866F7B9A96C05F5AC5C; Path=/; HttpOnly; SameSite=Lax로 나옴.

/auth/me를 실행했을 때, Request Headers 에 Cookie: JSESSIONID=B78674FB26AA2866F7B9A96C05F5AC5C 가 포함되어 있음. 위 Set-Cookie 값과 같고, 브라우저가 알아서 실어 보낸 것. 응답은 {"username":"demo"}

console 탭에서 직접 fetch를 불렀을 때 결과는 다음과 같다.

```
[404, false, { "message": "책을 찾을 수 없습니다: 999" }]
```

document.cookie 실행 시 JSESSIONID는 안 보임. HttpOnly 쿠키는 자바스크립트가 읽지 못하기 때문이고, Application 탭의 Cookies 에서만 확인할 수 있음 (XSS 로 세션 쿠키가 훔쳐지지 않게 하는 장치)

books 요청 우클릭 후 복사한 명령을 터미널에 붙여 넣었을 때 결과는 다음과 같다.

[{"id":1,"isbn":"1","title":"t","author":"a"}]

---
### 연습문제

1. curl -i localhost:8080/books/999 실행 결과, 상태 줄은 `HTTP/1.1 404`, Content-Type은 application/json
   `curl -s localhost:8080/books/999 | jq .` 결과
   ```
   {
     "message": "책을 찾을 수 없습니다: 999"
   }
   ```
2. 2단계 d 요청(title, author 빠뜨림)에 Accept-Language를 바꿔 보냈을 때
   - `ko` → `{"message":"author: 공백일 수 없습니다, title: 공백일 수 없습니다"}` [400]
   - `en` → `{"message":"author: must not be blank, title: must not be blank"}` [400]
   - 없는 책(`/books/999`)은 ko/en 모두 {"message":"책을 찾을 수 없습니다: 999"} 로 같음 (직접 작성한 문구라 헤더의 영향을 받지 않음)
   - 문제점: 문구가 언어 설정에 따라 바뀌므로 화면이 문구를 `===` 로 비교하면 깨지고, 그대로 보여 주면 사용자 언어와 맞지 않거나 `author:` 같은 필드명이 노출됨. 상태 코드와 `필드: ` 형식에 기대야 함
3. 세 번 실행: `200 0.058686` → `200 0.004373` → `200 0.004015`. 첫 요청이 가장 느리고 이후 줄어듦(워밍업)
   - `-w` 는 watch가 아니라 write-out. 응답이 끝난 뒤 `%{http_code}`, `%{time_total}` 같은 변수를 실제 값으로 치환해 출력함
   - `-o /dev/null` 은 본문을 버려서 숫자만 남기려는 용도
4. GET /books 의 Copy as fetch 를 Console 에 붙여 넣고 `.then((r) => r.json())` 으로 읽기
```
fetch("http://localhost:8080/books", {
  "headers": {
    "accept": "application/json"
  },
  "method": "GET",
  "credentials": "include"
}).then((r) => r.json());
```
결과: 배열로 파싱됨 (위 10번 줄의 응답과 같은 형태 `[{"id":1,"isbn":"1","title":"t","author":"a"}]`)

(처음에 붙였던 코드는 GET 이 아니라 Swagger 의 POST /books 요청이었음. GET 요청은 body 가 없고 method 가 GET 임)

5. 서버 끄고 Swagger 에서 GET /books 재실행
![스크린샷 2026-10-28 19.49.33.png](../../../../../../../var/folders/78/gfb1_3212nb_yn8kf2r4xx8w0000gn/T/TemporaryItems/NSIRD_screencaptureui_gvjivn/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202026-10-28%2019.49.33.png)

Network 탭에 (failed) net::ERR_CONNECTION_REFUSED 로 표시됨. 응답 자체가 없으므로 상태 코드가 없고(Status 열이 비어 있음), fetch 는 이때 예외를 던짐

### 퀴즈
1. 예외는 안 남. 404 응답이 정상으로 오므로 fetch 는 resolve 하고 res.status 는 404, res.ok 는 false. 예외는 응답을 받지 못했을 때(서버 꺼짐 등)만 남
2. 401은 "누구인지 확인 안 됨(로그인 필요)", 403은 "누구인지는 알지만 권한이 없음". 이 API 에서는 401 만 나오고 응답 message 로 구분함: "로그인이 필요합니다" = 미로그인, "아이디 또는 비밀번호가 올바르지 않습니다" = 로그인 실패
3. 요청한 경로는 있지만 그 메서드는 안 된다는 뜻. Allow: POST 가 쓸 수 있는 메서드를 알려 주므로 POST 로 바꿔 호출하면 됨
4. 버그가 아님. 쿠키에 HttpOnly 가 붙어 있어 자바스크립트가 못 읽게 막은 것이고, Application 탭에서는 보임
5. 쿠키. 브라우저는 로그인 때 받은 쿠키를 자동으로 보내지만 curl 은 -c 로 저장하고 -b 로 보내야 함