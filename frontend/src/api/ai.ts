import { http } from './http'

export interface TransactionDraft {
  type: string | null
  amount: number | null
  currency: string | null
  occurredAt: string | null
  merchant: string | null
  categoryName: string | null
  note: string | null
  needsReview: boolean
  rawInput: string | null
  validationMessage: string | null
}

export async function parseTransaction(spaceId: number, text: string): Promise<TransactionDraft> {
  const res = await http.post(`/api/ai/spaces/${spaceId}/parse-transaction`, { text })
  return res.data.data
}