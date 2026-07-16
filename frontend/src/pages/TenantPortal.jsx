import React, { useEffect, useState } from 'react';
import { get } from '../api.js';
import { Table } from '../components/ui/Table.jsx';
import { Alert } from '../components/ui/Alert.jsx';
import { CalendarDays, CreditCard, CheckCircle2, XCircle, Clock } from 'lucide-react';

function money(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0));
}

export function TenantPortal({ notify }) {
  const [contract, setContract] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [c, i] = await Promise.all([get('/tenant-portal/my-contract'), get('/tenant-portal/my-invoices')]);
        setContract(c);
        setInvoices(i);
      } catch (e) {
        setError(e.message);
      }
    })();
  }, []);

  const getStatusBadge = (status) => {
    switch(status?.toUpperCase()) {
      case 'PAID': return <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-400"><CheckCircle2 size={14}/> PAID</span>;
      case 'UNPAID': return <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-orange-100 text-orange-700 dark:bg-orange-900/40 dark:text-orange-400"><Clock size={14}/> UNPAID</span>;
      case 'CANCELED': return <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-400"><XCircle size={14}/> CANCELED</span>;
      case 'ACTIVE': return <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-400"><CheckCircle2 size={14}/> ACTIVE</span>;
      default: return <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300">{status}</span>;
    }
  }

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <Alert notice={error ? { type: 'error', message: error } : null} onClose={() => setError('')} />
      
      <div className="grid md:grid-cols-3 gap-6">
        {/* Contract Card */}
        {contract && (
          <div className="md:col-span-1">
            <div className="glass rounded-3xl p-6 relative overflow-hidden group">
              <div className="absolute top-0 right-0 p-6 opacity-20 group-hover:scale-110 transition-transform duration-500">
                <CreditCard size={80} className="text-white" />
              </div>
              <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
                My Contract
              </h2>
              <div className="space-y-4 relative z-10">
                <div className="bg-black/10 rounded-2xl p-4 border border-white/10">
                  <p className="text-sm text-white/70 mb-1">Room</p>
                  <p className="text-2xl font-black text-white">{contract.roomCode}</p>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-black/10 rounded-2xl p-4 border border-white/10">
                    <p className="text-sm text-white/70 mb-1">Start Date</p>
                    <p className="font-bold text-white">{contract.startDate}</p>
                  </div>
                  <div className="bg-black/10 rounded-2xl p-4 border border-white/10">
                    <p className="text-sm text-white/70 mb-1">Deposit</p>
                    <p className="font-bold text-white">{money(contract.depositAmount)}</p>
                  </div>
                </div>
                <div className="mt-4">
                  {getStatusBadge(contract.status)}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Invoices List */}
        <div className={`md:col-span-2 glass rounded-3xl p-6 flex flex-col ${!contract ? 'md:col-span-3' : ''}`}>
          <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
            <CalendarDays /> Invoice History
          </h2>
          <div className="flex-1 bg-slate-50 dark:bg-slate-950 rounded-2xl overflow-hidden shadow-inner">
            <Table 
              columns={['Month', 'Total Amount', 'Status']} 
              rows={invoices.map(i => [
                <span className="font-semibold text-slate-900 dark:text-slate-100">{i.billingMonth}</span>,
                <span className="font-bold text-slate-900 dark:text-slate-100">{money(i.totalAmount)}</span>,
                getStatusBadge(i.status)
              ])} 
            />
          </div>
        </div>
      </div>
    </div>
  );
}
