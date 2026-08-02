import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/auth.service';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Spinner from '../../components/ui/Spinner';
import { CheckCircle2, AlertCircle, Clock, AlertTriangle, LogIn, Send, Mail } from 'lucide-react';

const VerifyEmailPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('LOADING'); // LOADING, SUCCESS, ALREADY_VERIFIED, EXPIRED, INVALID
  const [email, setEmail] = useState('');
  
  // Resend state
  const [showResendInput, setShowResendInput] = useState(false);
  const [resendEmail, setResendEmail] = useState('');
  const [resendLoading, setResendLoading] = useState(false);
  const [resendMessage, setResendMessage] = useState('');
  const [resendError, setResendError] = useState('');

  // Ref to prevent double-execution in React 18 StrictMode
  const isVerifyingRef = useRef(false);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      setStatus('INVALID');
      return;
    }

    if (isVerifyingRef.current) return;
    isVerifyingRef.current = true;

    const verifyToken = async () => {
      try {
        const response = await authService.verifyEmail(token);
        const data = response.data || {};
        const resultStatus = data.status || (response.success ? 'SUCCESS' : 'INVALID');
        
        setStatus(resultStatus);
        if (data.email) {
          setEmail(data.email);
          setResendEmail(data.email);
        }
      } catch (err) {
        const msg = err.response?.data?.message || '';
        if (msg.toLowerCase().includes('already')) {
          setStatus('ALREADY_VERIFIED');
        } else if (msg.toLowerCase().includes('expired')) {
          setStatus('EXPIRED');
        } else {
          setStatus('INVALID');
        }
      } finally {
        setLoading(false);
      }
    };

    verifyToken();
  }, [token]);

  const handleResend = async (e) => {
    e?.preventDefault();
    const targetEmail = resendEmail || email;
    if (!targetEmail) {
      setResendError('Please enter your registered email address.');
      return;
    }

    setResendLoading(true);
    setResendError('');
    setResendMessage('');

    try {
      const response = await authService.resendVerification(targetEmail);
      if (response.success) {
        setResendMessage('Verification email sent successfully. Please check your inbox.');
      } else {
        setResendError(response.message || 'Failed to send verification email.');
      }
    } catch (err) {
      setResendError(err.response?.data?.message || 'Failed to send verification email.');
    } finally {
      setResendLoading(false);
    }
  };

  const handleGoToLogin = () => {
    navigate('/login');
  };

  return (
    <Card className="text-center py-8 px-6 shadow-md border-slate-200">
      {loading ? (
        <div className="py-8 space-y-4">
          <Spinner size="lg" className="mx-auto text-blue-600" />
          <h3 className="text-base font-semibold text-slate-900">Verifying Email Address...</h3>
          <p className="text-xs text-slate-500">Validating your token</p>
        </div>
      ) : status === 'SUCCESS' ? (
        /* ✅ Email Verified Successfully */
        <div className="space-y-4">
          <div className="mx-auto w-14 h-14 bg-emerald-50 border border-emerald-200 rounded-full flex items-center justify-center text-emerald-600">
            <CheckCircle2 className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold text-slate-900">✅ Email Verified Successfully</h3>
          <div className="text-xs text-slate-600 space-y-1 leading-relaxed px-2">
            <p>Your email has been verified successfully.</p>
            <p>Your SecureVault account is now active.</p>
            <p>You can now log in using your registered email and password.</p>
          </div>
          <div className="pt-3">
            <Button
              variant="emerald"
              size="lg"
              className="w-full"
              icon={LogIn}
              onClick={handleGoToLogin}
            >
              Go to Login
            </Button>
          </div>
        </div>
      ) : status === 'ALREADY_VERIFIED' ? (
        /* Email Already Verified */
        <div className="space-y-4">
          <div className="mx-auto w-14 h-14 bg-blue-50 border border-blue-200 rounded-full flex items-center justify-center text-blue-600">
            <CheckCircle2 className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold text-slate-900">Email Already Verified</h3>
          <p className="text-xs text-slate-600 leading-relaxed px-2">
            Your email has already been verified.
          </p>
          <div className="pt-3">
            <Button
              variant="primary"
              size="lg"
              className="w-full"
              icon={LogIn}
              onClick={handleGoToLogin}
            >
              Go to Login
            </Button>
          </div>
        </div>
      ) : status === 'EXPIRED' ? (
        /* Verification Link Expired */
        <div className="space-y-4">
          <div className="mx-auto w-14 h-14 bg-amber-50 border border-amber-200 rounded-full flex items-center justify-center text-amber-600">
            <Clock className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold text-slate-900">Verification Link Expired</h3>
          <p className="text-xs text-slate-600 leading-relaxed px-2">
            Please request another verification email.
          </p>

          {resendMessage && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-700 text-xs font-medium">
              {resendMessage}
            </div>
          )}

          {resendError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
              {resendError}
            </div>
          )}

          {showResendInput ? (
            <form onSubmit={handleResend} className="space-y-3 pt-2">
              <Input
                label="Registered Email Address"
                type="email"
                value={resendEmail}
                onChange={(e) => setResendEmail(e.target.value)}
                placeholder="Enter your email"
                icon={Mail}
                required
              />
              <Button
                type="submit"
                variant="primary"
                className="w-full"
                loading={resendLoading}
                icon={Send}
              >
                Send Verification Link
              </Button>
            </form>
          ) : (
            <div className="pt-3 space-y-2">
              <Button
                variant="primary"
                size="lg"
                className="w-full"
                icon={Send}
                loading={resendLoading}
                onClick={() => {
                  if (email) {
                    handleResend();
                  } else {
                    setShowResendInput(true);
                  }
                }}
              >
                Resend Verification
              </Button>
              <Button
                variant="secondary"
                className="w-full"
                onClick={handleGoToLogin}
              >
                Go to Login
              </Button>
            </div>
          )}
        </div>
      ) : (
        /* Invalid Verification Link */
        <div className="space-y-4">
          <div className="mx-auto w-14 h-14 bg-red-50 border border-red-200 rounded-full flex items-center justify-center text-red-600">
            <AlertCircle className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold text-slate-900">Invalid Verification Link</h3>
          <p className="text-xs text-slate-600 leading-relaxed px-2">
            The verification link is invalid. Please register again or request another verification email.
          </p>

          {resendMessage && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-700 text-xs font-medium">
              {resendMessage}
            </div>
          )}

          {resendError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
              {resendError}
            </div>
          )}

          {showResendInput ? (
            <form onSubmit={handleResend} className="space-y-3 pt-2">
              <Input
                label="Registered Email Address"
                type="email"
                value={resendEmail}
                onChange={(e) => setResendEmail(e.target.value)}
                placeholder="Enter your email"
                icon={Mail}
                required
              />
              <Button
                type="submit"
                variant="primary"
                className="w-full"
                loading={resendLoading}
                icon={Send}
              >
                Send Verification Email
              </Button>
            </form>
          ) : (
            <div className="pt-3 space-y-2">
              <Link to="/register">
                <Button variant="primary" className="w-full">
                  Register Again
                </Button>
              </Link>
              <Button
                variant="secondary"
                className="w-full"
                onClick={() => setShowResendInput(true)}
              >
                Request Verification Email
              </Button>
              <Button
                variant="outline"
                className="w-full text-slate-600"
                onClick={handleGoToLogin}
              >
                Go to Login
              </Button>
            </div>
          )}
        </div>
      )}
    </Card>
  );
};

export default VerifyEmailPage;
