# 빌드 컨텍스트는 이 폴더(labs/frontend)입니다. 사용법은 ../../fe-collab/week13-collab-practice.md 의 3.1절을 보세요.
FROM node:22
WORKDIR /app
RUN npm install -g pnpm@10
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
# NEXT_PUBLIC_* 는 아래 pnpm build 때 JS 번들에 값으로 박힙니다. 실행 때 -e 로 줘도 바뀌지 않습니다.
ARG NEXT_PUBLIC_API_BASE=
ENV NEXT_PUBLIC_API_BASE=$NEXT_PUBLIC_API_BASE
RUN pnpm build
ENV NODE_ENV=production
EXPOSE 3000
CMD ["node_modules/.bin/next", "start"]
