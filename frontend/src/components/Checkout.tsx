import { ArrowRight, ArrowLeft, CreditCard, Banknote } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useState } from 'react';

export function Checkout() {
    const { items, totalPrice, setView } = useCart();
    const [paymentMethod, setPaymentMethod] = useState<'cash' | 'transfer'>('cash');

    return (
        <div className="min-h-screen bg-[#0c0a09] text-stone-100 flex items-center justify-center p-4 md:p-8">
            <div className="w-full max-w-6xl bg-stone-900 border border-white/5 rounded-3xl overflow-hidden shadow-2xl flex flex-col md:flex-row">

                {/* Left: Summary */}
                <div className="flex-1 p-8 md:p-12 border-b md:border-b-0 md:border-r border-white/5 bg-stone-950/30">
                    <button
                        onClick={() => setView('store')}
                        className="flex items-center gap-2 text-stone-500 hover:text-orange-400 mb-8 transition-colors group"
                    >
                        <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" />
                        Retour à la boutique
                    </button>

                    <h2 className="text-3xl font-bold mb-8">Résumé de la commande</h2>

                    <div className="space-y-4 max-h-[400px] overflow-y-auto pr-2 custom-scrollbar">
                        {items.map(item => (
                            <div key={item.id} className="flex gap-4 items-center">
                                <div className="w-16 h-16 rounded-lg overflow-hidden bg-stone-800 shrink-0">
                                    <img src={item.image} alt={item.name} className="w-full h-full object-cover" />
                                </div>
                                <div className="flex-1">
                                    <h4 className="font-semibold text-stone-200">{item.name}</h4>
                                    <p className="text-stone-500 text-sm">Qté : {item.quantity}</p>
                                </div>
                                <span className="font-bold text-stone-300">{(item.price * item.quantity).toFixed(2)}€</span>
                            </div>
                        ))}
                    </div>

                    <div className="mt-8 pt-6 border-t border-white/10 space-y-2">
                        <div className="flex justify-between text-stone-400">
                            <span>Sous-total</span>
                            <span>{totalPrice.toFixed(2)}€</span>
                        </div>
                        <div className="flex justify-between text-2xl font-bold text-white mt-4">
                            <span>Total</span>
                            <span>{totalPrice.toFixed(2)}€</span>
                        </div>
                    </div>
                </div>

                {/* Right: Payment */}
                <div className="flex-1 p-8 bg-stone-900/50">
                    <h2 className="text-2xl font-bold mb-6">Moyen de paiement</h2>

                    <div className="space-y-4 mb-8">
                        <label className={`block p-4 rounded-xl border-2 cursor-pointer transition-all ${paymentMethod === 'cash' ? 'border-orange-500 bg-orange-500/10' : 'border-white/5 hover:border-white/20'}`}>
                            <div className="flex items-center gap-4">
                                <input
                                    type="radio"
                                    name="payment"
                                    checked={paymentMethod === 'cash'}
                                    onChange={() => setPaymentMethod('cash')}
                                    className="hidden"
                                />
                                <div className={`w-10 h-10 rounded-full flex items-center justify-center ${paymentMethod === 'cash' ? 'bg-orange-500 text-white' : 'bg-stone-800 text-stone-500'}`}>
                                    <Banknote size={20} />
                                </div>
                                <div>
                                    <h3 className="font-bold">Espèces / Contre-remboursement</h3>
                                    <p className="text-sm text-stone-500">Payez à la réception de votre commande</p>
                                </div>
                            </div>
                        </label>

                        <label className={`block p-4 rounded-xl border-2 cursor-pointer transition-all ${paymentMethod === 'transfer' ? 'border-orange-500 bg-orange-500/10' : 'border-white/5 hover:border-white/20'}`}>
                            <div className="flex items-center gap-4">
                                <input
                                    type="radio"
                                    name="payment"
                                    checked={paymentMethod === 'transfer'}
                                    onChange={() => setPaymentMethod('transfer')}
                                    className="hidden"
                                />
                                <div className={`w-10 h-10 rounded-full flex items-center justify-center ${paymentMethod === 'transfer' ? 'bg-orange-500 text-white' : 'bg-stone-800 text-stone-500'}`}>
                                    <CreditCard size={20} />
                                </div>
                                <div>
                                    <h3 className="font-bold">Virement bancaire</h3>
                                    <p className="text-sm text-stone-500">Envoyez le reçu par WhatsApp</p>
                                </div>
                            </div>
                        </label>
                    </div>

                    <button
                        onClick={() => setView('tracking')}
                        className="w-full bg-orange-600 hover:bg-orange-500 text-white p-4 rounded-xl font-bold flex items-center justify-center gap-2 transition-all shadow-lg shadow-orange-900/20 hover:scale-[1.02] active:scale-[0.98]"
                    >
                        <span>Confirmer la commande</span>
                        <ArrowRight size={20} />
                    </button>
                </div>
            </div>
        </div>
    );
}
