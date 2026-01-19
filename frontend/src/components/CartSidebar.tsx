import { X, Plus, Minus, ShoppingBag, Trash2, ArrowRight } from 'lucide-react';
import { useCart } from '../context/CartContext';

export function CartSidebar() {
    const { items, isOpen, toggleCart, updateQuantity, removeFromCart, totalPrice, setView } = useCart();

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex justify-end">
            {/* Overlay Backdrop */}
            <div
                className="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity"
                onClick={toggleCart}
            />

            {/* Sidebar Panel */}
            <div className="relative w-full max-w-md bg-stone-900 h-full shadow-2xl border-l border-white/10 flex flex-col transform transition-transform duration-300">

                {/* Header */}
                <div className="p-6 border-b border-white/5 flex items-center justify-between bg-stone-950/50">
                    <div className="flex items-center gap-3">
                        <ShoppingBag className="text-orange-500" />
                        <h2 className="text-xl font-bold text-white">Votre commande</h2>
                    </div>
                    <button
                        onClick={toggleCart}
                        className="p-2 hover:bg-white/10 rounded-full transition-colors text-stone-400 hover:text-white"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Cart Items List */}
                <div className="flex-1 overflow-y-auto p-6 space-y-6">
                    {items.length === 0 ? (
                        <div className="h-full flex flex-col items-center justify-center text-center space-y-4 opacity-50">
                            <ShoppingBag size={48} className="text-stone-600" />
                            <p className="text-stone-400">Votre panier est vide</p>
                            <button
                                onClick={toggleCart}
                                className="text-orange-500 font-semibold hover:underline"
                            >
                                Explorer les produits
                            </button>
                        </div>
                    ) : (
                        items.map((item) => (
                            <div key={item.id} className="flex gap-4 bg-stone-950/30 p-4 rounded-2xl border border-white/5 group hover:border-orange-500/20 transition-colors">
                                {/* Product Image */}
                                <div className="w-20 h-20 rounded-xl overflow-hidden bg-stone-800 shrink-0">
                                    <img src={item.image} alt={item.name} className="w-full h-full object-cover" />
                                </div>

                                {/* Info & Controls */}
                                <div className="flex-1 flex flex-col justify-between">
                                    <div className="flex justify-between items-start">
                                        <h3 className="font-semibold text-stone-200 line-clamp-1">{item.name}</h3>
                                        <button
                                            onClick={() => removeFromCart(item.id)}
                                            className="text-stone-500 hover:text-red-400 transition-colors p-1"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>

                                    <div className="flex justify-between items-end">
                                        <div className="flex items-center gap-3 bg-stone-900 rounded-lg p-1 border border-white/5">
                                            <button
                                                onClick={() => updateQuantity(item.id, -1)}
                                                className="p-1 hover:bg-white/10 rounded-md text-stone-400 hover:text-white disabled:opacity-50"
                                            >
                                                <Minus size={14} />
                                            </button>
                                            <span className="text-sm font-medium w-4 text-center">{item.quantity}</span>
                                            <button
                                                onClick={() => updateQuantity(item.id, 1)}
                                                className="p-1 hover:bg-white/10 rounded-md text-stone-400 hover:text-white"
                                            >
                                                <Plus size={14} />
                                            </button>
                                        </div>
                                        <span className="font-bold text-white">{(item.price * item.quantity).toFixed(2)}€</span>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {/* Footer & Checkout */}
                {items.length > 0 && (
                    <div className="p-6 border-t border-white/10 bg-stone-950">
                        <div className="space-y-3 mb-6">
                            <div className="flex justify-between text-stone-400">
                                <span>Sous-total</span>
                                <span>{totalPrice.toFixed(2)}€</span>
                            </div>
                            <div className="flex justify-between text-xl font-bold text-white">
                                <span>Total</span>
                                <span>{totalPrice.toFixed(2)}€</span>
                            </div>
                        </div>

                        <button
                            onClick={() => {
                                toggleCart();
                                setView('checkout');
                            }}
                            className="w-full bg-orange-600 hover:bg-orange-500 text-white p-4 rounded-xl font-bold flex items-center justify-center gap-2 transition-all shadow-lg shadow-orange-900/20 hover:scale-[1.02] active:scale-[0.98]"
                        >
                            <span>Passer à la caisse</span>
                            <ArrowRight size={20} />
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
