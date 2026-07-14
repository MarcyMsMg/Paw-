import { notificationsApi } from "@/services/notificationsApi";
import { safeStat, statsApi } from "@/services/statsApi";

const adoptionFallback = { totalApplications: 0, pendingApplications: 0, acceptedApplications: 0, rejectedApplications: 0, applicationsByStatus: {}, animalsAvailable: 0, animalsAdopted: 0, animalsByStatus: {} };
const campaignFallback = { activeCampaigns: 0, finishedCampaigns: 0, totalGoalAmount: 0, totalRaisedAmount: 0, averageProgress: 0, campaignProgress: [] };
const donationFallback = { totalDonated: 0, totalRaised: 0, approvedDonations: 0, pendingDonations: 0, rejectedDonations: 0, donationsByMonth: {} };
const adminFallback = { totalUsers: 0, activeUsers: 0, pendingNgoRequests: 0, activeNgos: 0, naturalPersons: 0, usersByRole: {}, usersByStatus: {} };

export const dashboardApi = {
  async ngo(userId) {
    const [adoptions, campaigns, donations, notifications] = await Promise.all([
      safeStat(() => statsApi.getAdoptionStatsNgo(), adoptionFallback),
      safeStat(() => statsApi.getCampaignStatsNgo(userId), campaignFallback),
      safeStat(() => statsApi.getDonationStatsNgo(userId), donationFallback),
      safeStat(() => notificationsApi.listMyNotifications({ limit: 5 }), []),
    ]);
    return { adoptions, campaigns, donations, notifications };
  },
  async person(userId) {
    const [adoptions, donations, notifications] = await Promise.all([
      safeStat(() => statsApi.getAdoptionStatsPerson(), adoptionFallback),
      safeStat(() => statsApi.getDonationStatsPerson(userId), donationFallback),
      safeStat(() => notificationsApi.listMyNotifications({ limit: 5 }), []),
    ]);
    return { adoptions, donations, notifications };
  },
  async admin() {
    const [users, adoptions, campaigns, donations, notifications] = await Promise.all([
      safeStat(() => statsApi.getAdminUserStats(), adminFallback),
      safeStat(() => statsApi.getAdoptionStatsAdmin(), adoptionFallback),
      safeStat(() => statsApi.getCampaignStatsAdmin(), campaignFallback),
      safeStat(() => statsApi.getDonationStatsAdmin(), donationFallback),
      safeStat(() => notificationsApi.listMyNotifications({ limit: 5 }), []),
    ]);
    return { users, adoptions, campaigns, donations, notifications };
  },
};