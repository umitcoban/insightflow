export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly details?: unknown
  ) {
    super(message);
  }
}

export function toMessage(error: unknown) {
  if (error instanceof ApiError) {
    const detail = typeof error.details === "object" && error.details && "detail" in error.details
      ? String((error.details as { detail?: unknown }).detail)
      : error.message;
    return `${detail} (${error.status})`;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Unexpected error";
}
