import type { InputHTMLAttributes } from "react";

type Props = InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string };

export function TextField({ label, error, id, ...input }: Props) {
  const fieldId = id ?? `field-${input.name}`;
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={fieldId} className="font-medium">
        {label}
      </label>
      <input
        id={fieldId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? `${fieldId}-error` : undefined}
        className="w-full rounded-card border border-line bg-surface p-field text-fg aria-invalid:border-warning"
        {...input}
      />
      {error && (
        <p id={`${fieldId}-error`} className="text-warning">
          {error}
        </p>
      )}
    </div>
  );
}
