import Link from "next/link";

const LINKS = [
  { href: "/", label: "책" },
  { href: "/members", label: "회원" },
  { href: "/loans", label: "대출·반납" },
];

export function Nav() {
  return (
    <nav aria-label="주 메뉴" className="flex gap-gutter">
      {LINKS.map((l) => (
        <Link key={l.href} href={l.href} className="font-semibold text-primary underline-offset-4 hover:underline">
          {l.label}
        </Link>
      ))}
    </nav>
  );
}
