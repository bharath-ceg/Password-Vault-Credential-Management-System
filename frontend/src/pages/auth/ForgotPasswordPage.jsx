import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../../services/auth.service';
import { useNotification } from '../../context/NotificationContext';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import { Mail, KeyRound, ArrowRight, ShieldCheck } from 'lucide-react';

const ForgotPasswordPage = () => {
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { showToast } = useNotification();
  const navigate = useNavigate();

  const handleSendOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authService.forgotPassword(email);
      if (response.success) {
        setStep(2);
        showToast('A 5-minute single-use OTP has been emailed to you.', 'success');
      } else {
        setError(response.message || 'Failed to send OTP.');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'No registered user found with this email.';
      setError(msg);
      showToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authService.verifyOtp(email, otpCode);
      if (response.success) {
        showToast('OTP verified successfully! Please enter your new password.', 'success');
        navigate('/reset-password', { state: { email, otpCode } });
      } else {
        setError('Invalid or Expired OTP.');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Invalid or Expired OTP.';
      setError(msg);
      showToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card className="shadow-md border-slate-200">
      <div className="mb-6 text-center">
        <h3 className="text-xl font-bold text-slate-900">Reset Password</h3>
        <p className="text-xs text-slate-500 mt-1">
          {step === 1
            ? 'Enter your registered email address to receive an OTP code'
            : `Enter the 6-digit OTP code sent to ${email}`}
        </p>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
          {error}
        </div>
      )}

      {step === 1 ? (
        <form onSubmit={handleSendOtp} className="space-y-4">
          <Input
            label="Email Address"
            type="email"
            name="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              setError('');
            }}
            placeholder="Enter your email address"
            icon={Mail}
            required
          />

          <Button
            type="submit"
            variant="primary"
            size="lg"
            className="w-full mt-2"
            loading={loading}
            icon={ArrowRight}
          >
            Send Verification OTP
          </Button>
        </form>
      ) : (
        <form onSubmit={handleVerifyOtp} className="space-y-4">
          <Input
            label="6-Digit OTP Security Code"
            type="text"
            name="otpCode"
            maxLength={6}
            value={otpCode}
            onChange={(e) => {
              setOtpCode(e.target.value);
              setError('');
            }}
            placeholder="Enter 6-digit OTP"
            icon={KeyRound}
            required
          />

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs text-slate-600">
            <strong>Expiration:</strong> This security code expires in 5 minutes.
          </div>

          <Button
            type="submit"
            variant="emerald"
            size="lg"
            className="w-full mt-2"
            loading={loading}
            icon={ShieldCheck}
          >
            Verify OTP Code
          </Button>

          <button
            type="button"
            onClick={() => setStep(1)}
            className="w-full text-xs text-slate-500 hover:text-slate-800 transition-colors py-1 cursor-pointer"
          >
            Change Email Address
          </button>
        </form>
      )}

      <div className="mt-6 pt-4 border-t border-slate-100 text-center text-xs text-slate-500">
        Remembered your password?{' '}
        <Link to="/login" className="text-blue-600 hover:underline font-semibold">
          Sign In
        </Link>
      </div>
    </Card>
  );
};

export default ForgotPasswordPage;
