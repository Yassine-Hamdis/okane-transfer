import { Pipe, PipeTransform } from '@angular/core';
import { TransferStatus }      from '../../core/models/transfer.model';

const STATUS_MAP: Record<string, string> = {
  EN_ATTENTE: 'Pending',
  PAYE:       'Paid',
  ANNULE:     'Cancelled',
  EXPIRE:     'Expired',
};

@Pipe({
  name: 'transferStatus',
  standalone: true,
})
export class TransferStatusPipe implements PipeTransform {
  transform(value: TransferStatus | string | null | undefined): string {
    if (!value) return '—';
    return STATUS_MAP[value] ?? value;
  }
}
