import React, { useState, useEffect, useCallback } from 'react';
import { generatorService } from '../../services/generator.service';
import { useNotification } from '../../context/NotificationContext';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import {
  KeyRound,
  Copy,
  Check,
  RefreshCw,
  ShieldCheck,
  ShieldAlert,
  Sliders,
  CheckSquare,
  Square,
  AlertCircle
} from 'lucide-react';

const PasswordGeneratorPage = () => {
  const [length, setLength] = useState(16);
  const [useUpper, setUseUpper] = useState(true);
  const [useLower, setUseLower] = useState(true);
  const [useNumbers, setUseNumbers] = useState(true);
  const [useSpecial, setUseSpecial] = useState(true);

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  const { showToast } = useNotification();

  const handleGenerate = useCallback(async () => {
    if (!useUpper && !useLower && !useNumbers && !useSpecial) {
      setError('Please select at least one character type (Uppercase, Lowercase, Numbers, or Special characters).');
      setResult(null);
      return;
    }

    setError('');
    setLoading(true);

    try {
      const response = await generatorService.generatePassword({
        length: Number(length),
        useUpper,
        useLower,
        useNumbers,
        useSpecial,
      });

      if (response.success && response.data) {
        setResult(response.data);
      } else {
        setError(response.message || 'Failed to generate password.');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate password. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [length, useUpper, useLower, useNumbers, useSpecial]);

  useEffect(() => {
    handleGenerate();
  }, [handleGenerate]);

  const handleCopy = () => {
    if (!result?.password) return;
    navigator.clipboard.writeText(result.password);
    setCopied(true);
    showToast('Password copied to clipboard!', 'success');
    setTimeout(() => setCopied(false), 2000);
  };

  const getStrengthBadge = (label) => {
    switch (label) {
      case 'Very Strong':
        return { bg: 'bg-emerald-50 text-emerald-700 border-emerald-200', bar: 'bg-emerald-500', width: 'w-full' };
      case 'Strong':
        return { bg: 'bg-blue-50 text-blue-700 border-blue-200', bar: 'bg-blue-600', width: 'w-3/4' };
      case 'Fair':
        return { bg: 'bg-amber-50 text-amber-700 border-amber-200', bar: 'bg-amber-500', width: 'w-1/2' };
      case 'Weak':
      default:
        return { bg: 'bg-red-50 text-red-700 border-red-200', bar: 'bg-red-500', width: 'w-1/4' };
    }
  };

  const strengthBadge = result ? getStrengthBadge(result.strengthLabel) : null;

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
          <KeyRound className="w-6 h-6 text-blue-600" /> Password Generator
        </h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Generate cryptographically secure passwords and analyze password strength
        </p>
      </div>

      {/* Main Card */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Controls */}
        <Card className="lg:col-span-2 space-y-6 border-slate-200">
          {/* Password Output Area */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">
              Generated Password
            </label>

            <div className="relative flex items-center bg-slate-50 border border-slate-200 rounded-xl p-3 sm:p-3.5 shadow-xs">
              <input
                type="text"
                readOnly
                value={result?.password || ''}
                placeholder="Click generate to create password"
                className="w-full bg-transparent font-mono text-sm sm:text-base md:text-lg font-bold text-slate-900 focus:outline-none pr-28 sm:pr-32 tracking-wide truncate"
              />

              <div className="absolute right-2 flex items-center gap-1">
                <button
                  type="button"
                  onClick={handleGenerate}
                  disabled={loading}
                  className="p-1.5 sm:p-2 text-slate-500 hover:text-blue-600 hover:bg-slate-200/60 rounded-lg transition-colors cursor-pointer"
                  title="Regenerate"
                >
                  <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-blue-600' : ''}`} />
                </button>
                <Button
                  variant={copied ? 'success' : 'primary'}
                  size="sm"
                  onClick={handleCopy}
                  disabled={!result?.password}
                  icon={copied ? Check : Copy}
                  className="px-2.5 sm:px-3 text-xs"
                >
                  {copied ? 'Copied' : 'Copy'}
                </Button>
              </div>
            </div>

            {error && (
              <div className="mt-2 p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700 font-medium flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0" />
                <span>{error}</span>
              </div>
            )}
          </div>

          {/* Strength Meter */}
          {result && (
            <div className="space-y-2 pt-2 border-t border-slate-100">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold text-slate-600">Password Strength:</span>
                <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold border uppercase tracking-wider ${strengthBadge.bg}`}>
                  {result.strengthLabel} ({result.strengthScore}/100)
                </span>
              </div>

              {/* Meter Bar */}
              <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden border border-slate-200">
                <div className={`h-full transition-all duration-300 ${strengthBadge.bar} ${strengthBadge.width}`} />
              </div>
            </div>
          )}

          {/* Controls: Length & Character Options */}
          <div className="space-y-5 pt-4 border-t border-slate-100">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-2">
              <Sliders className="w-4 h-4 text-blue-600" /> Generator Options
            </h4>

            {/* Length Slider */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <label className="text-xs font-medium text-slate-700">Password Length</label>
                <span className="px-2.5 py-0.5 bg-blue-50 border border-blue-200 text-blue-700 font-mono font-bold text-xs rounded-md">
                  {length} characters
                </span>
              </div>
              <input
                type="range"
                min="8"
                max="64"
                value={length}
                onChange={(e) => setLength(e.target.value)}
                className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
              />
              <div className="flex justify-between text-[10px] text-slate-400 font-mono">
                <span>8</span>
                <span>24</span>
                <span>40</span>
                <span>64</span>
              </div>
            </div>

            {/* Character Set Options */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
              <button
                type="button"
                onClick={() => setUseUpper(!useUpper)}
                className={`p-3 rounded-lg border text-left flex items-center justify-between transition-colors cursor-pointer ${
                  useUpper
                    ? 'bg-blue-50/50 border-blue-200 text-blue-900 font-semibold'
                    : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <div className="text-xs">
                  <p className="font-medium">Uppercase Letters</p>
                  <p className="text-[11px] text-slate-400 font-mono">A-Z</p>
                </div>
                {useUpper ? <CheckSquare className="w-4 h-4 text-blue-600" /> : <Square className="w-4 h-4 text-slate-300" />}
              </button>

              <button
                type="button"
                onClick={() => setUseLower(!useLower)}
                className={`p-3 rounded-lg border text-left flex items-center justify-between transition-colors cursor-pointer ${
                  useLower
                    ? 'bg-blue-50/50 border-blue-200 text-blue-900 font-semibold'
                    : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <div className="text-xs">
                  <p className="font-medium">Lowercase Letters</p>
                  <p className="text-[11px] text-slate-400 font-mono">a-z</p>
                </div>
                {useLower ? <CheckSquare className="w-4 h-4 text-blue-600" /> : <Square className="w-4 h-4 text-slate-300" />}
              </button>

              <button
                type="button"
                onClick={() => setUseNumbers(!useNumbers)}
                className={`p-3 rounded-lg border text-left flex items-center justify-between transition-colors cursor-pointer ${
                  useNumbers
                    ? 'bg-blue-50/50 border-blue-200 text-blue-900 font-semibold'
                    : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <div className="text-xs">
                  <p className="font-medium">Numbers</p>
                  <p className="text-[11px] text-slate-400 font-mono">0-9</p>
                </div>
                {useNumbers ? <CheckSquare className="w-4 h-4 text-blue-600" /> : <Square className="w-4 h-4 text-slate-300" />}
              </button>

              <button
                type="button"
                onClick={() => setUseSpecial(!useSpecial)}
                className={`p-3 rounded-lg border text-left flex items-center justify-between transition-colors cursor-pointer ${
                  useSpecial
                    ? 'bg-blue-50/50 border-blue-200 text-blue-900 font-semibold'
                    : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <div className="text-xs">
                  <p className="font-medium">Special Characters</p>
                  <p className="text-[11px] text-slate-400 font-mono">!@#$%^&amp;*</p>
                </div>
                {useSpecial ? <CheckSquare className="w-4 h-4 text-blue-600" /> : <Square className="w-4 h-4 text-slate-300" />}
              </button>
            </div>

            <Button
              variant="primary"
              className="w-full py-2.5 text-sm"
              onClick={handleGenerate}
              loading={loading}
              icon={RefreshCw}
            >
              Generate Password
            </Button>
          </div>
        </Card>

        {/* Right Column: Strength Suggestions & Security Info */}
        <div className="space-y-5">
          <Card className="border-slate-200 space-y-4">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900 flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-600" /> Security Recommendations
            </h4>

            {result?.suggestions && result.suggestions.length > 0 ? (
              <ul className="space-y-2.5 text-xs text-slate-600">
                {result.suggestions.map((sug, idx) => (
                  <li key={idx} className="flex items-start gap-2 bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                    <span className="text-blue-600 font-bold">•</span>
                    <span>{sug}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-xs text-slate-500">Configure parameters above to analyze password security.</p>
            )}
          </Card>

          <Card className="bg-blue-50/50 border-blue-100 text-blue-900 space-y-2">
            <h5 className="text-xs font-bold flex items-center gap-1.5">
              <ShieldAlert className="w-4 h-4 text-blue-600" /> Cryptographic Security
            </h5>
            <p className="text-[11px] leading-relaxed text-blue-800">
              Passwords generated by SecureVault utilize backend-backed cryptographically secure pseudo-random number generators (PRNG) to ensure optimal entropy and protection against brute-force attacks.
            </p>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default PasswordGeneratorPage;
