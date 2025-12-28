// Helper to get ISO strings for today's dates
const today = new Date();
const getISO = (minusHours) => {
    const d = new Date(today);
    d.setHours(d.getHours() - minusHours);
    return d.toISOString();
};

export const mockAlerts = [
    {
        id: '1',
        userName: 'John Doe',
        alertType: 'SOS',
        userCategory: 'Blind',
        isVulnerable: true,
        timestamp: getISO(1),
        status: 'Active',
        description: 'User reported smoke and fire in the apartment building. Needs immediate evacuation assistance due to vision impairment.'
    },
    {
        id: '2',
        userName: 'Mary Smith',
        alertType: 'Injured',
        userCategory: 'Elderly',
        isVulnerable: true,
        timestamp: getISO(2),
        status: 'Pending',
        description: 'Fall detected. User is unable to get up. Hip pain reported. High priority.'
    },
    {
        id: '3',
        userName: 'Robert Brown',
        alertType: 'Trapped',
        userCategory: 'Deaf',
        isVulnerable: true,
        timestamp: getISO(3),
        status: 'Active',
        description: 'Stuck in elevator during power outage. Communication via text only. No injuries but breathing is heavy.'
    },
    {
        id: '4',
        userName: 'Emma Wilson',
        alertType: 'Safe',
        userCategory: 'Normal',
        isVulnerable: false,
        timestamp: getISO(4),
        status: 'Resolved',
        description: 'Checking in as safe after the earthquake tremor.'
    },
    {
        id: '5',
        userName: 'James Lee',
        alertType: 'SOS',
        userCategory: 'Non-verbal',
        isVulnerable: true,
        timestamp: getISO(5),
        status: 'Pending',
        description: 'Emergency alert triggered. User is non-verbal. GPS location shows near river bank during flood warning.'
    },
    {
        id: '6',
        userName: 'Sarah Garcia',
        alertType: 'Injured',
        userCategory: 'Normal',
        isVulnerable: false,
        timestamp: getISO(6),
        status: 'Active',
        description: 'Minor leg injury from debris. Can walk but slowly.'
    }
];
