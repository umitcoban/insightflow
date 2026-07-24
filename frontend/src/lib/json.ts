export function parseJsonObject(value: string, label = "JSON"): Record<string, unknown> {
  if (!value.trim()) {
    return {};
  }
  const parsed = JSON.parse(value);
  if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") {
    throw new Error(`${label} must be a JSON object.`);
  }
  return parsed as Record<string, unknown>;
}

export function parseJsonArray(value: string, label = "JSON"): Array<Record<string, unknown>> {
  if (!value.trim()) {
    return [];
  }
  const parsed = JSON.parse(value);
  if (!Array.isArray(parsed)) {
    throw new Error(`${label} must be a JSON array.`);
  }
  return parsed as Array<Record<string, unknown>>;
}

export function prettyJson(value: unknown) {
  return JSON.stringify(value, null, 2);
}
