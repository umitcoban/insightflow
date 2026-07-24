export function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString() : "Not available";
}

export function shortId(value?: string) {
  return value ? value.slice(0, 8) : "pending";
}
