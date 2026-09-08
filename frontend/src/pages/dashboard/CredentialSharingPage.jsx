import React, { useState, useEffect, useCallback } from 'react';
import { sharingService } from '../../services/sharing.service';
import { vaultService } from '../../services/vault.service';
import { useNotification } from '../../context/NotificationContext';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import {
  Users,
  Share2,
  Plus,
  Eye,
  Trash2,
  Edit,
  Globe,
  User,
  Clock,
  ShieldCheck,
  ShieldAlert,
  Lock,
  Copy,
  Check,
  Mail,
  Calendar,
  AlertCircle
} from 'lucide-react';

const PERMISSIONS = [
  {
    id: 'VIEW_ONLY',
    label: 'View Only',
    desc: 'Can view credential details & reveal password. Cannot edit, delete, or manage sharing.'
  },
  {
    id: 'EDIT_ACCESS',
    label: 'Edit Access',
    desc: 'Can view and edit credential details. Cannot delete or manage sharing.'
  },
  {
    id: 'FULL_MANAGEMENT',
    label: 'Full Management',
    desc: 'Can view, edit, delete, and manage sharing with other users.'
  }
];

const CredentialSharingPage = () => {
  const [activeTab, setActiveTab] = useState('SHARED_WITH_ME'); // SHARED_WITH_ME | MY_SHARES

  const [myShares, setMyShares] = useState([]);
  const [sharedWithMe, setSharedWithMe] = useState([]);
  const [myCredentials, setMyCredentials] = useState([]);
  const [loading, setLoading] = useState(true);

  // Share Modal State
  const [isShareModalOpen, setIsShareModalOpen] = useState(false);
  const [shareForm, setShareForm] = useState({
    credentialId: '',
    recipientEmail: '',
    permission: 'VIEW_ONLY',
    expiresAt: ''
  });
  const [shareLoading, setShareLoading] = useState(false);
  const [shareError, setShareError] = useState('');

  // Update Share Modal State
  const [editingShare, setEditingShare] = useState(null);
  const [updateForm, setUpdateForm] = useState({ permission: 'VIEW_ONLY', expiresAt: '' });
  const [updateLoading, setUpdateLoading] = useState(false);
  const [updateError, setUpdateError] = useState('');

  // Revoke Share Modal State
  const [revokeItem, setRevokeItem] = useState(null);
  const [revokeLoading, setRevokeLoading] = useState(false);

  // Reveal Password Modal State
  const [revealModalShare, setRevealModalShare] = useState(null);
  const [privacyPassword, setPrivacyPassword] = useState('');
  const [revealedPassword, setRevealedPassword] = useState('');
  const [revealLoading, setRevealLoading] = useState(false);
  const [revealError, setRevealError] = useState('');

  // Edit Credential Modal (for shared credential edit when permitted)
  const [editingCredential, setEditingCredential] = useState(null);
  const [editForm, setEditForm] = useState({ applicationUrl: '', aliasName: '', username: '', password: '' });
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState('');

  // Delete Credential Modal
  const [deleteModalCredentialId, setDeleteModalCredentialId] = useState(null);

  const [copiedId, setCopiedId] = useState(null);
  const { showToast } = useNotification();

  const fetchSharingData = useCallback(async () => {
    setLoading(true);
    try {
      const [mySharesRes, sharedWithMeRes, vaultRes] = await Promise.all([
        sharingService.getMyShares(),
        sharingService.getSharedWithMe(),
        vaultService.getCredentials()
      ]);

      if (mySharesRes.success) setMyShares(mySharesRes.data || []);
      if (sharedWithMeRes.success) setSharedWithMe(sharedWithMeRes.data || []);
      if (vaultRes.success) setMyCredentials(vaultRes.data || []);
    } catch {
      showToast('Failed to load sharing details.', 'error');
    } finally {
      setLoading(false);
    }
  }, [showToast]);

  useEffect(() => {
    fetchSharingData();
  }, [fetchSharingData]);

  // Create Share Submit
  const handleShareSubmit = async (e) => {
    e.preventDefault();
    if (!shareForm.credentialId) {
      setShareError('Please select a credential to share.');
      return;
    }
    if (!shareForm.recipientEmail) {
      setShareError('Please enter recipient email.');
      return;
    }

    setShareLoading(true);
    setShareError('');

    try {
      const payload = {
        credentialId: Number(shareForm.credentialId),
        recipientEmail: shareForm.recipientEmail.trim(),
        permission: shareForm.permission,
        expiresAt: shareForm.expiresAt ? new Date(shareForm.expiresAt).toISOString() : null
      };

      const res = await sharingService.createShare(payload);
      if (res.success) {
        showToast('Credential shared successfully!', 'success');
        setIsShareModalOpen(false);
        setShareForm({ credentialId: '', recipientEmail: '', permission: 'VIEW_ONLY', expiresAt: '' });
        fetchSharingData();
      }
    } catch (err) {
      setShareError(err.response?.data?.message || 'Failed to share credential.');
    } finally {
      setShareLoading(false);
    }
  };

  // Update Share Submit
  const handleUpdateShareSubmit = async (e) => {
    e.preventDefault();
    if (!editingShare) return;

    setUpdateLoading(true);
    setUpdateError('');

    try {
      const payload = {
        permission: updateForm.permission,
        expiresAt: updateForm.expiresAt ? new Date(updateForm.expiresAt).toISOString() : null
      };

      const res = await sharingService.updateShare(editingShare.id, payload);
      if (res.success) {
        showToast('Share permission updated.', 'success');
        setEditingShare(null);
        fetchSharingData();
      }
    } catch (err) {
      setUpdateError(err.response?.data?.message || 'Failed to update share.');
    } finally {
      setUpdateLoading(false);
    }
  };

  // Revoke Share Submit
  const handleRevokeShare = async () => {
    if (!revokeItem) return;
    setRevokeLoading(true);
    try {
      const res = await sharingService.revokeShare(revokeItem.id);
      if (res.success) {
        showToast('Credential share revoked.', 'success');
        setRevokeItem(null);
        fetchSharingData();
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to revoke share.', 'error');
    } finally {
      setRevokeLoading(false);
    }
  };

  // Reveal Shared Password
  const handleRevealSharedPassword = async (e) => {
    e.preventDefault();
    if (!revealModalShare) return;

    setRevealLoading(true);
    setRevealError('');

    try {
      const res = await sharingService.revealSharedPassword(revealModalShare.id, privacyPassword);
      if (res.success && res.data) {
        setRevealedPassword(res.data);
        showToast('Password decrypted & revealed', 'success');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Incorrect Privacy Password or expired share.';
      setRevealError(msg);
    } finally {
      setRevealLoading(false);
    }
  };

  // Handle Edit Shared Credential
  const handleOpenEditCredential = (share) => {
    setEditingCredential(share);
    setEditForm({
      applicationUrl: share.credentialUrl || '',
      aliasName: share.credentialAlias || '',
      username: share.username || '',
      password: ''
    });
    setEditError('');
  };

  const handleEditCredentialSubmit = async (e) => {
    e.preventDefault();
    if (!editingCredential) return;

    setEditLoading(true);
    setEditError('');

    try {
      const res = await vaultService.updateCredential(editingCredential.credentialId, editForm);
      if (res.success) {
        showToast('Credential updated successfully!', 'success');
        setEditingCredential(null);
        fetchSharingData();
      }
    } catch (err) {
      setEditError(err.response?.data?.message || 'Failed to update credential.');
    } finally {
      setEditLoading(false);
    }
  };

  // Handle Delete Shared Credential
  const handleDeleteSharedCredential = async () => {
    if (!deleteModalCredentialId) return;
    try {
      const res = await vaultService.deleteCredential(deleteModalCredentialId);
      if (res.success) {
        showToast('Credential deleted.', 'success');
        setDeleteModalCredentialId(null);
        fetchSharingData();
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete credential.', 'error');
    }
  };

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    showToast('Copied to clipboard!', 'info');
    setTimeout(() => setCopiedId(null), 2000);
  };

  const getPermissionBadgeClass = (perm) => {
    switch (perm) {
      case 'FULL_MANAGEMENT': return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'EDIT_ACCESS': return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'VIEW_ONLY':
      default: return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  const formatExpiration = (expiresAt, isExpired) => {
    if (!expiresAt) return { text: 'Permanent', class: 'bg-emerald-50 text-emerald-700 border-emerald-200' };
    if (isExpired) return { text: 'Expired', class: 'bg-red-50 text-red-700 border-red-200' };
    
    const expDate = new Date(expiresAt);
    const diffHours = (expDate - new Date()) / (1000 * 60 * 60);
    if (diffHours <= 24) {
      return { text: `Expiring Soon (${expDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })})`, class: 'bg-amber-50 text-amber-700 border-amber-200' };
    }
    return { text: expDate.toLocaleDateString(), class: 'bg-blue-50 text-blue-700 border-blue-200' };
  };

  return (
    <div className="space-y-6">
      {/* Header & Primary Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <Users className="w-6 h-6 text-blue-600" /> Credential Sharing
          </h2>
          <p className="text-xs text-slate-500">
            Securely share vault credentials with team members with permission and temporary expiration control
          </p>
        </div>

        <Button
          variant="primary"
          icon={Plus}
          className="w-full sm:w-auto"
          onClick={() => {
            setShareError('');
            setShareForm({ credentialId: '', recipientEmail: '', permission: 'VIEW_ONLY', expiresAt: '' });
            setIsShareModalOpen(true);
          }}
        >
          Share Credential
        </Button>
      </div>

      {/* Navigation View Tabs */}
      <div className="flex overflow-x-auto border-b border-slate-200 gap-4 sm:gap-6">
        <button
          onClick={() => setActiveTab('SHARED_WITH_ME')}
          className={`pb-3 text-xs sm:text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors cursor-pointer whitespace-nowrap ${
            activeTab === 'SHARED_WITH_ME'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-500 hover:text-slate-900'
          }`}
        >
          <Share2 className="w-4 h-4" /> Shared With Me
          <span className="px-2 py-0.5 text-xs bg-slate-100 text-slate-600 rounded-full font-bold">
            {sharedWithMe.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('MY_SHARES')}
          className={`pb-3 text-xs sm:text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors cursor-pointer whitespace-nowrap ${
            activeTab === 'MY_SHARES'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-500 hover:text-slate-900'
          }`}
        >
          <Users className="w-4 h-4" /> My Shared Credentials
          <span className="px-2 py-0.5 text-xs bg-slate-100 text-slate-600 rounded-full font-bold">
            {myShares.length}
          </span>
        </button>
      </div>

      {/* Main Content Area */}
      {loading ? (
        <div className="py-16 text-center">
          <Spinner size="lg" className="mx-auto text-blue-600" />
          <p className="text-xs text-slate-500 mt-3">Loading credential shares...</p>
        </div>
      ) : activeTab === 'SHARED_WITH_ME' ? (
        /* SHARED WITH ME LIST */
        sharedWithMe.length === 0 ? (
          <Card className="text-center py-16 px-4 border-slate-200">
            <div className="mx-auto w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mb-3">
              <Share2 className="w-6 h-6" />
            </div>
            <h4 className="text-base font-bold text-slate-900">No Credentials Shared With You</h4>
            <p className="text-xs text-slate-500 mt-1">
              When other users share credentials with you, they will appear in this section.
            </p>
          </Card>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4 sm:gap-5">
            {sharedWithMe.map((item) => {
              const expStatus = formatExpiration(item.expiresAt, item.isExpired);
              const canEdit = !item.isExpired && (item.permission === 'EDIT_ACCESS' || item.permission === 'FULL_MANAGEMENT');
              const canDelete = !item.isExpired && item.permission === 'FULL_MANAGEMENT';

              return (
                <Card key={item.id} hover className="flex flex-col justify-between space-y-4 border-slate-200">
                  <div>
                    {/* Header Info */}
                    <div className="flex items-start justify-between gap-2 mb-3">
                      <div className="flex items-center gap-3 overflow-hidden">
                        <div className="w-9 h-9 rounded-lg bg-blue-50 border border-blue-100 flex items-center justify-center text-blue-700 font-bold text-sm flex-shrink-0">
                          {item.credentialAlias?.charAt(0).toUpperCase() || 'S'}
                        </div>
                        <div className="overflow-hidden">
                          <h4 className="font-bold text-slate-900 text-sm truncate">{item.credentialAlias}</h4>
                          <p className="text-[11px] text-slate-500 truncate flex items-center gap-1">
                            <User className="w-3 h-3 text-slate-400" /> Shared by: {item.ownerEmail}
                          </p>
                        </div>
                      </div>

                      <span className={`px-2 py-0.5 text-[10px] font-semibold rounded-md border uppercase whitespace-nowrap ${getPermissionBadgeClass(item.permission)}`}>
                        {item.permission?.replace('_', ' ')}
                      </span>
                    </div>

                    {/* Username & Expiration */}
                    <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg space-y-2 text-xs">
                      <div className="flex items-center justify-between text-slate-700">
                        <span className="flex items-center gap-1.5 truncate font-medium">
                          <User className="w-3.5 h-3.5 text-slate-400" /> {item.username}
                        </span>
                        <button
                          onClick={() => copyToClipboard(item.username, `user-${item.id}`)}
                          className="text-slate-400 hover:text-slate-700 p-1"
                          title="Copy Username"
                        >
                          {copiedId === `user-${item.id}` ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                        </button>
                      </div>

                      <div className="flex items-center justify-between pt-1 border-t border-slate-200/60 text-[11px]">
                        <span className="text-slate-500 flex items-center gap-1">
                          <Clock className="w-3 h-3 text-slate-400" /> Expiration:
                        </span>
                        <span className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${expStatus.class}`}>
                          {expStatus.text}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                    <Button
                      variant={item.isExpired ? 'secondary' : 'emerald'}
                      size="sm"
                      icon={Eye}
                      disabled={item.isExpired}
                      onClick={() => {
                        setRevealModalShare(item);
                        setPrivacyPassword('');
                        setRevealedPassword('');
                        setRevealError('');
                      }}
                    >
                      {item.isExpired ? 'Expired' : 'Reveal'}
                    </Button>

                    <div className="flex items-center gap-1">
                      {canEdit && (
                        <button
                          onClick={() => handleOpenEditCredential(item)}
                          className="p-1.5 text-slate-500 hover:text-slate-900 rounded-md hover:bg-slate-100 cursor-pointer"
                          title="Edit Credential"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                      )}

                      {canDelete && (
                        <button
                          onClick={() => setDeleteModalCredentialId(item.credentialId)}
                          className="p-1.5 text-slate-500 hover:text-red-600 rounded-md hover:bg-red-50 cursor-pointer"
                          title="Delete Credential"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        )
      ) : (
        /* MY SHARED CREDENTIALS LIST */
        myShares.length === 0 ? (
          <Card className="text-center py-16 px-4 border-slate-200">
            <div className="mx-auto w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mb-3">
              <Users className="w-6 h-6" />
            </div>
            <h4 className="text-base font-bold text-slate-900">No Active Shares Created</h4>
            <p className="text-xs text-slate-500 mt-1 mb-4">
              You haven't shared any credentials from your vault yet.
            </p>
            <Button
              variant="primary"
              size="sm"
              icon={Plus}
              onClick={() => {
                setShareError('');
                setShareForm({ credentialId: '', recipientEmail: '', permission: 'VIEW_ONLY', expiresAt: '' });
                setIsShareModalOpen(true);
              }}
            >
              Share Credential Now
            </Button>
          </Card>
        ) : (
          <div className="overflow-x-auto bg-white border border-slate-200 rounded-xl shadow-xs">
            <table className="w-full text-left text-xs text-slate-700 border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 uppercase text-[10px] font-bold text-slate-500 tracking-wider">
                  <th className="py-3 px-4">Credential</th>
                  <th className="py-3 px-4">Recipient</th>
                  <th className="py-3 px-4">Permission</th>
                  <th className="py-3 px-4">Expiration</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {myShares.map((share) => {
                  const expStatus = formatExpiration(share.expiresAt, share.isExpired);

                  return (
                    <tr key={share.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="py-3 px-4 font-semibold text-slate-900">
                        {share.credentialAlias}
                      </td>
                      <td className="py-3 px-4">
                        <div>
                          <p className="font-semibold text-slate-900">{share.recipientFullName}</p>
                          <p className="text-[11px] text-slate-400">{share.recipientEmail}</p>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span className={`px-2.5 py-0.5 text-[10px] font-bold rounded-md border uppercase ${getPermissionBadgeClass(share.permission)}`}>
                          {share.permission?.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${expStatus.class}`}>
                          {expStatus.text}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => {
                              setEditingShare(share);
                              setUpdateForm({
                                permission: share.permission,
                                expiresAt: share.expiresAt ? new Date(share.expiresAt).toISOString().slice(0, 16) : ''
                              });
                              setUpdateError('');
                            }}
                            className="p-1.5 text-slate-500 hover:text-blue-600 rounded-md hover:bg-blue-50 cursor-pointer"
                            title="Edit Permission / Expiration"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => setRevokeItem(share)}
                            className="p-1.5 text-slate-500 hover:text-red-600 rounded-md hover:bg-red-50 cursor-pointer"
                            title="Revoke Share"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )
      )}

      {/* Modal 1: Share Credential */}
      <Modal
        isOpen={isShareModalOpen}
        onClose={() => setIsShareModalOpen(false)}
        title="Share Vault Credential"
      >
        <form onSubmit={handleShareSubmit} className="space-y-4">
          {shareError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium flex items-center gap-2">
              <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0" />
              <span>{shareError}</span>
            </div>
          )}

          {/* Select Credential */}
          <div className="space-y-1">
            <label className="block text-xs font-semibold text-slate-700">Select Credential</label>
            <select
              value={shareForm.credentialId}
              onChange={(e) => setShareForm({ ...shareForm, credentialId: e.target.value })}
              className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500 shadow-xs"
              required
            >
              <option value="">-- Choose a Vault Credential --</option>
              {myCredentials.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.aliasName || c.applicationUrl} ({c.username})
                </option>
              ))}
            </select>
          </div>

          {/* Target Recipient Email */}
          <Input
            label="Recipient Email"
            type="email"
            value={shareForm.recipientEmail}
            onChange={(e) => setShareForm({ ...shareForm, recipientEmail: e.target.value })}
            placeholder="enter.registered.user@email.com"
            icon={Mail}
            required
          />

          {/* Permission Level Selector */}
          <div className="space-y-2">
            <label className="block text-xs font-semibold text-slate-700">Permission Level</label>
            <div className="space-y-2">
              {PERMISSIONS.map((perm) => (
                <label
                  key={perm.id}
                  className={`flex items-start gap-3 p-3 rounded-lg border cursor-pointer transition-colors ${
                    shareForm.permission === perm.id
                      ? 'bg-blue-50/50 border-blue-200 text-blue-900 font-semibold'
                      : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  <input
                    type="radio"
                    name="permission"
                    value={perm.id}
                    checked={shareForm.permission === perm.id}
                    onChange={(e) => setShareForm({ ...shareForm, permission: e.target.value })}
                    className="mt-0.5 text-blue-600 accent-blue-600"
                  />
                  <div>
                    <p className="text-xs font-bold">{perm.label}</p>
                    <p className="text-[11px] text-slate-500 font-normal mt-0.5">{perm.desc}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Expiration DateTime (Optional) */}
          <Input
            label="Temporary Expiration Date & Time (Optional)"
            type="datetime-local"
            value={shareForm.expiresAt}
            onChange={(e) => setShareForm({ ...shareForm, expiresAt: e.target.value })}
            icon={Calendar}
          />
          <p className="text-[11px] text-slate-400">
            Leave blank for permanent sharing. Once expired, recipient access is automatically revoked.
          </p>

          <div className="pt-2 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setIsShareModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={shareLoading} icon={Share2}>
              Create Share
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal 2: Edit Share Permission / Expiration */}
      <Modal
        isOpen={!!editingShare}
        onClose={() => setEditingShare(null)}
        title="Update Share Settings"
      >
        <form onSubmit={handleUpdateShareSubmit} className="space-y-4">
          {updateError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
              {updateError}
            </div>
          )}

          <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs space-y-1">
            <p className="font-bold text-slate-900">{editingShare?.credentialAlias}</p>
            <p className="text-slate-500">Shared with: {editingShare?.recipientEmail}</p>
          </div>

          <div className="space-y-2">
            <label className="block text-xs font-semibold text-slate-700">Permission Level</label>
            <div className="space-y-2">
              {PERMISSIONS.map((perm) => (
                <label
                  key={perm.id}
                  className={`flex items-start gap-3 p-3 rounded-lg border cursor-pointer transition-colors ${
                    updateForm.permission === perm.id
                      ? 'bg-blue-50/50 border-blue-200 text-blue-900 font-semibold'
                      : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  <input
                    type="radio"
                    name="updatePermission"
                    value={perm.id}
                    checked={updateForm.permission === perm.id}
                    onChange={(e) => setUpdateForm({ ...updateForm, permission: e.target.value })}
                    className="mt-0.5 text-blue-600 accent-blue-600"
                  />
                  <div>
                    <p className="text-xs font-bold">{perm.label}</p>
                    <p className="text-[11px] text-slate-500 font-normal mt-0.5">{perm.desc}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          <Input
            label="Expiration Date & Time (Optional)"
            type="datetime-local"
            value={updateForm.expiresAt}
            onChange={(e) => setUpdateForm({ ...updateForm, expiresAt: e.target.value })}
            icon={Calendar}
          />

          <div className="pt-2 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setEditingShare(null)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={updateLoading}>
              Save Settings
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal 3: Revoke Share Confirmation */}
      <Modal
        isOpen={!!revokeItem}
        onClose={() => setRevokeItem(null)}
        title="Revoke Credential Share"
      >
        <div className="space-y-4">
          <p className="text-xs text-slate-600 leading-relaxed">
            Are you sure you want to revoke share access for <strong>{revokeItem?.recipientEmail}</strong> to <strong>{revokeItem?.credentialAlias}</strong>? The recipient will immediately lose access.
          </p>

          <div className="pt-2 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setRevokeItem(null)}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleRevokeShare} loading={revokeLoading} icon={Trash2}>
              Revoke Share
            </Button>
          </div>
        </div>
      </Modal>

      {/* Modal 4: Reveal Shared Password */}
      <Modal
        isOpen={!!revealModalShare}
        onClose={() => {
          setRevealModalShare(null);
          setRevealedPassword('');
          setRevealError('');
        }}
        title={`Reveal Password for ${revealModalShare?.credentialAlias}`}
      >
        {revealedPassword ? (
          <div className="space-y-4 text-center py-2">
            <p className="text-xs text-slate-500">Decrypted Secret Password:</p>
            <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg flex items-center justify-between font-mono text-lg text-emerald-800 font-bold">
              <span>{revealedPassword}</span>
              <button
                onClick={() => copyToClipboard(revealedPassword, 'revealed')}
                className="text-emerald-700 hover:text-emerald-900 p-2 cursor-pointer"
                title="Copy Password"
              >
                {copiedId === 'revealed' ? <Check className="w-5 h-5 text-emerald-600" /> : <Copy className="w-5 h-5" />}
              </button>
            </div>
            <Button
              variant="secondary"
              className="w-full mt-4"
              onClick={() => {
                setRevealModalShare(null);
                setRevealedPassword('');
                setRevealError('');
              }}
            >
              Close
            </Button>
          </div>
        ) : (
          <form onSubmit={handleRevealSharedPassword} className="space-y-4">
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-start gap-2.5 text-xs text-amber-900">
              <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
              <span>
                Enter your Privacy Password to decrypt and reveal this shared credential secret.
              </span>
            </div>

            {revealError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
                {revealError}
              </div>
            )}

            <Input
              label="Privacy Password"
              type="password"
              value={privacyPassword}
              onChange={(e) => {
                setPrivacyPassword(e.target.value);
                setRevealError('');
              }}
              placeholder="Enter your privacy password"
              icon={Lock}
              required
            />

            <div className="pt-2 flex justify-end gap-3">
              <Button variant="secondary" onClick={() => setRevealModalShare(null)}>
                Cancel
              </Button>
              <Button type="submit" variant="emerald" loading={revealLoading}>
                Decrypt Password
              </Button>
            </div>
          </form>
        )}
      </Modal>

      {/* Modal 5: Edit Shared Credential */}
      <Modal
        isOpen={!!editingCredential}
        onClose={() => setEditingCredential(null)}
        title={`Edit Credential - ${editingCredential?.credentialAlias}`}
      >
        <form onSubmit={handleEditCredentialSubmit} className="space-y-4">
          {editError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
              {editError}
            </div>
          )}

          <Input
            label="Application URL"
            type="text"
            value={editForm.applicationUrl}
            onChange={(e) => setEditForm({ ...editForm, applicationUrl: e.target.value })}
            placeholder="Enter application URL"
            icon={Globe}
          />

          <Input
            label="Alias Name"
            type="text"
            value={editForm.aliasName}
            onChange={(e) => setEditForm({ ...editForm, aliasName: e.target.value })}
            placeholder="Enter alias name"
          />

          <Input
            label="Username / Email"
            type="text"
            value={editForm.username}
            onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
            placeholder="Enter username"
            icon={User}
            required
          />

          <Input
            label="New Password (Optional)"
            type="password"
            value={editForm.password}
            onChange={(e) => setEditForm({ ...editForm, password: e.target.value })}
            placeholder="Enter new password to update secret"
            icon={Lock}
          />

          <div className="pt-2 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setEditingCredential(null)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={editLoading}>
              Update Credential
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal 6: Delete Shared Credential Confirmation */}
      <Modal
        isOpen={!!deleteModalCredentialId}
        onClose={() => setDeleteModalCredentialId(null)}
        title="Delete Shared Credential"
      >
        <div className="space-y-4">
          <p className="text-xs text-slate-600">
            Are you sure you want to permanently delete this credential? Full Management permission allows deleting this shared entry.
          </p>

          <div className="pt-2 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setDeleteModalCredentialId(null)}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleDeleteSharedCredential} icon={Trash2}>
              Delete Credential
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default CredentialSharingPage;
