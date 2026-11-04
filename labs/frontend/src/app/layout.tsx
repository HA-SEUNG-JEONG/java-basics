import type { Metadata } from "next";
import type { ReactNode } from "react";
import { AuthBar } from "@/components/AuthBar";
import { Nav } from "@/components/Nav";
import { Providers } from "./providers";
import "./globals.css";

export const metadata: Metadata = { title: "도서관", description: "도서관 REST API 클라이언트" };

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <Providers>
          <div className="mx-auto flex w-full max-w-page flex-col gap-stack p-gutter">
            <header className="flex flex-col gap-gutter border-b border-line pb-gutter">
              <h1 className="text-2xl font-bold">도서관</h1>
              <Nav />
              <AuthBar />
            </header>
            <main className="flex flex-col gap-stack">{children}</main>
          </div>
        </Providers>
      </body>
    </html>
  );
}
