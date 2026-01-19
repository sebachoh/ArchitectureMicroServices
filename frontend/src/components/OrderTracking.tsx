import { CheckCircle2, MapPin, ChefHat, Truck, Home } from 'lucide-react';
import { useCart } from '../context/CartContext';

export function OrderTracking() {
    const { setView } = useCart();

    const steps = [
        { icon: CheckCircle2, label: "Confirmé", time: "15:30", active: true },
        { icon: ChefHat, label: "En préparation", time: "15:45", active: true },
        { icon: Truck, label: "En route", time: "16:10", active: false },
        { icon: MapPin, label: "Livré", time: "16:25", active: false },
    ];

    return (
        <div className="min-h-screen bg-[#0c0a09] text-stone-100 flex items-center justify-center p-4">
            <div className="max-w-2xl w-full text-center">

                <div className="mb-12">
                    <div className="w-20 h-20 bg-green-500/20 text-green-500 rounded-full flex items-center justify-center mx-auto mb-6 animate-in zoom-in duration-500">
                        <CheckCircle2 size={40} />
                    </div>
                    <h1 className="text-4xl font-bold mb-4 bg-gradient-to-r from-white to-stone-400 bg-clip-text text-transparent">
                        Commande Confirmée !
                    </h1>
                    <p className="text-stone-400 text-lg">
                        Merci pour votre achat. Vos empanadas sont entre de bonnes mains.
                    </p>
                </div>

                <div className="bg-stone-900 border border-white/5 rounded-3xl p-8 mb-12 shadow-2xl relative overflow-hidden">
                    <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-orange-500/50 to-transparent"></div>

                    <div className="relative">
                        {/* Connecting Line */}
                        <div className="absolute top-6 left-0 w-full h-0.5 bg-stone-800 -z-10"></div>

                        <div className="flex justify-between">
                            {steps.map((step, index) => {
                                const Icon = step.icon;
                                return (
                                    <div key={index} className="flex flex-col items-center gap-4">
                                        <div className={`w-12 h-12 rounded-full flex items-center justify-center border-4 border-stone-900 z-10 transition-colors ${step.active ? 'bg-orange-500 text-white' : 'bg-stone-800 text-stone-500'}`}>
                                            <Icon size={20} />
                                        </div>
                                        <div>
                                            <h4 className={`font-bold text-sm ${step.active ? 'text-white' : 'text-stone-500'}`}>{step.label}</h4>
                                            <p className="text-xs text-stone-600 mt-1">{step.time}</p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>

                <button
                    onClick={() => setView('store')}
                    className="bg-stone-800 hover:bg-stone-700 text-white px-8 py-3 rounded-xl font-bold inline-flex items-center gap-2 transition-all"
                >
                    <Home size={18} />
                    Retour à la boutique
                </button>
            </div>
        </div>
    );
}
