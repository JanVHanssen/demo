// services/HelloService.ts
export async function getGreeting(): Promise<string> {
  const response = await fetch("http://localhost:8080/hello");

  if (!response.ok) {
    throw new Error(`API returned ${response.status}`);
  }

  const data = await response.text();
  return data;
}
