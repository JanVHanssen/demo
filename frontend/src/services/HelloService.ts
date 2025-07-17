// services/HelloService.ts
export async function getGreeting(): Promise<string> {
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/hello`);

  if (!response.ok) {
    throw new Error(`API returned ${response.status}`);
  }

  const data = await response.text();
  return data;
}
