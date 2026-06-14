import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyFormat',
  standalone: true,
})
export class CurrencyFormatPipe implements PipeTransform {
  /**
   * Usage in template:
   *   {{ 1500 | currencyFormat:'MAD' }}  →  "1,500.00 MAD"
   *   {{ 200  | currencyFormat:'EUR':'€' }}  →  "€200.00"
   */
  transform(
    value: number | null | undefined,
    currencyCode = '',
    symbol       = '',
  ): string {
    if (value === null || value === undefined) return '—';

    const formatted = new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value);

    if (symbol) return `${symbol}${formatted}`;
    if (currencyCode) return `${formatted} ${currencyCode}`;
    return formatted;
  }
}
