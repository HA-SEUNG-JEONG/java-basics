"use client";

import { useEffect, useState, type ReactNode } from "react";
import { worker } from "./browser";

// 개발 모드의 React StrictMode 는 effect 를 두 번 실행한다. worker.start() 는 한 번만 호출해야 한다.
let starting: Promise<unknown> | undefined;

/** 서비스 워커가 준비된 뒤에 자식을 그린다(첫 요청부터 가짜 응답을 받도록). */
export default function MockGate({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);
  useEffect(() => {
    starting ??= worker.start({ onUnhandledFrame: "bypass" });
    starting.then(() => setReady(true));
  }, []);
  return ready ? children : null;
}
