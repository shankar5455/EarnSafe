package com.earnsafe.config;

import com.earnsafe.entity.*;
import com.earnsafe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final RiskZoneRepository riskZoneRepository;
    private final WeatherEventRepository weatherEventRepository;
    private final DeliveryActivityRepository deliveryActivityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled || userRepository.count() > 0) {
            log.info("Seed data already exists or seeding is disabled. Skipping.");
            return;
        }

        log.info("Seeding database with initial data...");

        seedRiskZones();
        List<User> users = seedUsers();
        List<Policy> policies = seedPolicies(users);
        seedWeatherEvents();
        seedClaims(users, policies);
        seedDeliveryActivities(users);

        log.info("Database seeding complete!");
    }

    private void seedRiskZones() {
        List<RiskZone> zones = List.of(
            RiskZone.builder().city("Hyderabad").zone("Kukatpally").floodRiskScore(8).rainRiskScore(7).heatRiskScore(9).pollutionRiskScore(6).closureRiskScore(4).overallRiskLevel("HIGH").build(),
            RiskZone.builder().city("Hyderabad").zone("Banjara Hills").floodRiskScore(4).rainRiskScore(5).heatRiskScore(8).pollutionRiskScore(5).closureRiskScore(3).overallRiskLevel("MEDIUM").build(),
            RiskZone.builder().city("Hyderabad").zone("LB Nagar").floodRiskScore(9).rainRiskScore(8).heatRiskScore(7).pollutionRiskScore(7).closureRiskScore(5).overallRiskLevel("HIGH").build(),
            RiskZone.builder().city("Vijayawada").zone("Governorpet").floodRiskScore(9).rainRiskScore(9).heatRiskScore(8).pollutionRiskScore(6).closureRiskScore(5).overallRiskLevel("HIGH").build(),
            RiskZone.builder().city("Vijayawada").zone("Benz Circle").floodRiskScore(7).rainRiskScore(8).heatRiskScore(7).pollutionRiskScore(5).closureRiskScore(4).overallRiskLevel("HIGH").build(),
            RiskZone.builder().city("Visakhapatnam").zone("Gajuwaka").floodRiskScore(6).rainRiskScore(7).heatRiskScore(6).pollutionRiskScore(8).closureRiskScore(3).overallRiskLevel("MEDIUM").build(),
            RiskZone.builder().city("Visakhapatnam").zone("Dwaraka Nagar").floodRiskScore(5).rainRiskScore(6).heatRiskScore(5).pollutionRiskScore(5).closureRiskScore(2).overallRiskLevel("MEDIUM").build(),
            RiskZone.builder().city("Guntur").zone("Brodipet").floodRiskScore(7).rainRiskScore(8).heatRiskScore(9).pollutionRiskScore(6).closureRiskScore(3).overallRiskLevel("HIGH").build(),
            RiskZone.builder().city("Tirupati").zone("Tirumala Road").floodRiskScore(3).rainRiskScore(4).heatRiskScore(8).pollutionRiskScore(4).closureRiskScore(6).overallRiskLevel("MEDIUM").build(),
            RiskZone.builder().city("Tirupati").zone("Renigunta").floodRiskScore(4).rainRiskScore(5).heatRiskScore(9).pollutionRiskScore(5).closureRiskScore(3).overallRiskLevel("MEDIUM").build()
        );
        riskZoneRepository.saveAll(zones);
    }

    private List<User> seedUsers() {
        User admin = User.builder()
                .fullName("EarnSafe Admin")
                .phone("9000000000")
                .email("admin@earnsafe.com")
                .password(passwordEncoder.encode("Admin@123"))
                .deliveryPlatform("EarnSafe")
                .deliveryCategory("Platform")
                .city("Hyderabad")
                .zone("Banjara Hills")
                .pincode("500034")
                .preferredShift("Morning")
                .averageDailyEarnings(new BigDecimal("0"))
                .averageWorkingHours(new BigDecimal("8"))
                .vehicleType("Car")
                .role(User.Role.ADMIN)
                .build();

        User worker1 = User.builder()
                .fullName("Ravi Kumar")
                .phone("9876543210")
                .email("ravi@example.com")
                .password(passwordEncoder.encode("Worker@123"))
                .deliveryPlatform("Swiggy")
                .deliveryCategory("Food")
                .city("Hyderabad")
                .zone("Kukatpally")
                .pincode("500072")
                .preferredShift("Evening")
                .averageDailyEarnings(new BigDecimal("650"))
                .averageWorkingHours(new BigDecimal("7"))
                .vehicleType("Bike")
                .role(User.Role.WORKER)
                .build();

        User worker2 = User.builder()
                .fullName("Priya Sharma")
                .phone("9876543211")
                .email("priya@example.com")
                .password(passwordEncoder.encode("Worker@123"))
                .deliveryPlatform("Zomato")
                .deliveryCategory("Food")
                .city("Vijayawada")
                .zone("Governorpet")
                .pincode("520002")
                .preferredShift("Night")
                .averageDailyEarnings(new BigDecimal("720"))
                .averageWorkingHours(new BigDecimal("8"))
                .vehicleType("Scooter")
                .role(User.Role.WORKER)
                .build();

        User worker3 = User.builder()
                .fullName("Suresh Reddy")
                .phone("9876543212")
                .email("suresh@example.com")
                .password(passwordEncoder.encode("Worker@123"))
                .deliveryPlatform("Zepto")
                .deliveryCategory("Grocery")
                .city("Hyderabad")
                .zone("LB Nagar")
                .pincode("500074")
                .preferredShift("Morning")
                .averageDailyEarnings(new BigDecimal("580"))
                .averageWorkingHours(new BigDecimal("6"))
                .vehicleType("EV")
                .role(User.Role.WORKER)
                .build();

        User worker4 = User.builder()
                .fullName("Anjali Patel")
                .phone("9876543213")
                .email("anjali@example.com")
                .password(passwordEncoder.encode("Worker@123"))
                .deliveryPlatform("Blinkit")
                .deliveryCategory("Grocery")
                .city("Visakhapatnam")
                .zone("Gajuwaka")
                .pincode("530026")
                .preferredShift("Afternoon")
                .averageDailyEarnings(new BigDecimal("500"))
                .averageWorkingHours(new BigDecimal("6"))
                .vehicleType("Cycle")
                .role(User.Role.WORKER)
                .build();

        User worker5 = User.builder()
                .fullName("Mohammed Rafiq")
                .phone("9876543214")
                .email("rafiq@example.com")
                .password(passwordEncoder.encode("Worker@123"))
                .deliveryPlatform("Amazon")
                .deliveryCategory("E-commerce")
                .city("Guntur")
                .zone("Brodipet")
                .pincode("522002")
                .preferredShift("Morning")
                .averageDailyEarnings(new BigDecimal("850"))
                .averageWorkingHours(new BigDecimal("9"))
                .vehicleType("Bike")
                .role(User.Role.WORKER)
                .build();

        return userRepository.saveAll(List.of(admin, worker1, worker2, worker3, worker4, worker5));
    }

    private List<Policy> seedPolicies(List<User> users) {
        // users[0] = admin (skip), users[1..5] = workers
        Policy p1 = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(users.get(1))
                .planName("Standard Weekly Plan")
                .weeklyPremium(new BigDecimal("59"))
                .weeklyCoverageAmount(new BigDecimal("590"))
                .coveredHours(40)
                .coveredDisruptions("HEAVY_RAIN,FLOOD_ALERT,HEATWAVE,POLLUTION_SPIKE,ZONE_CLOSURE")
                .zoneCovered("Kukatpally")
                .status(Policy.PolicyStatus.ACTIVE)
                .riskScore("HIGH")
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().plusDays(21))
                .build();

        Policy p2 = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(users.get(2))
                .planName("Premium Night Shield")
                .weeklyPremium(new BigDecimal("71"))
                .weeklyCoverageAmount(new BigDecimal("710"))
                .coveredHours(48)
                .coveredDisruptions("HEAVY_RAIN,FLOOD_ALERT,HEATWAVE,POLLUTION_SPIKE,ZONE_CLOSURE")
                .zoneCovered("Governorpet")
                .status(Policy.PolicyStatus.ACTIVE)
                .riskScore("HIGH")
                .startDate(LocalDate.now().minusDays(3))
                .endDate(LocalDate.now().plusDays(25))
                .build();

        Policy p3 = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(users.get(3))
                .planName("Flood Guard Plan")
                .weeklyPremium(new BigDecimal("63"))
                .weeklyCoverageAmount(new BigDecimal("630"))
                .coveredHours(36)
                .coveredDisruptions("HEAVY_RAIN,FLOOD_ALERT,ZONE_CLOSURE")
                .zoneCovered("LB Nagar")
                .status(Policy.PolicyStatus.ACTIVE)
                .riskScore("HIGH")
                .startDate(LocalDate.now().minusDays(14))
                .endDate(LocalDate.now().plusDays(14))
                .build();

        Policy p4 = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(users.get(4))
                .planName("Basic Protection Plan")
                .weeklyPremium(new BigDecimal("46"))
                .weeklyCoverageAmount(new BigDecimal("460"))
                .coveredHours(32)
                .coveredDisruptions("HEAVY_RAIN,FLOOD_ALERT")
                .zoneCovered("Gajuwaka")
                .status(Policy.PolicyStatus.PAUSED)
                .riskScore("MEDIUM")
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(18))
                .build();

        Policy p5 = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(users.get(5))
                .planName("High Earner Plus Plan")
                .weeklyPremium(new BigDecimal("79"))
                .weeklyCoverageAmount(new BigDecimal("850"))
                .coveredHours(54)
                .coveredDisruptions("HEAVY_RAIN,FLOOD_ALERT,HEATWAVE,POLLUTION_SPIKE,ZONE_CLOSURE")
                .zoneCovered("Brodipet")
                .status(Policy.PolicyStatus.ACTIVE)
                .riskScore("HIGH")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(27))
                .build();

        return policyRepository.saveAll(List.of(p1, p2, p3, p4, p5));
    }

    private void seedWeatherEvents() {
        List<WeatherEvent> events = List.of(
            WeatherEvent.builder().city("Hyderabad").zone("Kukatpally").eventType("HEAVY_RAIN").temperature(new BigDecimal("28")).rainfallMm(new BigDecimal("45")).aqi(150).floodAlert(false).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(1)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Hyderabad").zone("LB Nagar").eventType("FLOOD_ALERT").temperature(new BigDecimal("27")).rainfallMm(new BigDecimal("80")).aqi(180).floodAlert(true).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(2)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Vijayawada").zone("Governorpet").eventType("HEAVY_RAIN").temperature(new BigDecimal("26")).rainfallMm(new BigDecimal("55")).aqi(120).floodAlert(false).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(1)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Guntur").zone("Brodipet").eventType("HEATWAVE").temperature(new BigDecimal("46")).rainfallMm(new BigDecimal("0")).aqi(200).floodAlert(false).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(3)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Visakhapatnam").zone("Gajuwaka").eventType("POLLUTION_SPIKE").temperature(new BigDecimal("32")).rainfallMm(new BigDecimal("5")).aqi(320).floodAlert(false).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(4)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Hyderabad").zone("Banjara Hills").eventType("ZONE_CLOSURE").temperature(new BigDecimal("30")).rainfallMm(new BigDecimal("0")).aqi(90).floodAlert(false).closureAlert(true).eventTimestamp(LocalDateTime.now().minusDays(5)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Tirupati").zone("Tirumala Road").eventType("HEATWAVE").temperature(new BigDecimal("43")).rainfallMm(new BigDecimal("0")).aqi(110).floodAlert(false).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(2)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Vijayawada").zone("Benz Circle").eventType("FLOOD_ALERT").temperature(new BigDecimal("25")).rainfallMm(new BigDecimal("95")).aqi(140).floodAlert(true).closureAlert(true).eventTimestamp(LocalDateTime.now().minusDays(6)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Hyderabad").zone("Kukatpally").eventType("POLLUTION_SPIKE").temperature(new BigDecimal("33")).rainfallMm(new BigDecimal("0")).aqi(350).floodAlert(false).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(7)).sourceType("MOCK").build(),
            WeatherEvent.builder().city("Guntur").zone("Brodipet").eventType("HEAVY_RAIN").temperature(new BigDecimal("29")).rainfallMm(new BigDecimal("60")).aqi(160).floodAlert(false).closureAlert(false).eventTimestamp(LocalDateTime.now().minusDays(8)).sourceType("MOCK").build()
        );
        weatherEventRepository.saveAll(events);
    }

    private void seedClaims(List<User> users, List<Policy> policies) {
        List<Claim> claims = List.of(
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(1)).policy(policies.get(0)).triggerType("HEAVY_RAIN").disruptionDate(LocalDate.now().minusDays(1)).city("Hyderabad").zone("Kukatpally").estimatedLostHours(new BigDecimal("4.9")).estimatedLostIncome(new BigDecimal("455")).validationStatus("AUTO_APPROVED").claimStatus(Claim.ClaimStatus.APPROVED).fraudFlag(false).payoutAmount(new BigDecimal("455")).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(1)).policy(policies.get(0)).triggerType("FLOOD_ALERT").disruptionDate(LocalDate.now().minusDays(2)).city("Hyderabad").zone("Kukatpally").estimatedLostHours(new BigDecimal("6.3")).estimatedLostIncome(new BigDecimal("585")).validationStatus("PAID").claimStatus(Claim.ClaimStatus.PAID).fraudFlag(false).payoutAmount(new BigDecimal("585")).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(2)).policy(policies.get(1)).triggerType("HEAVY_RAIN").disruptionDate(LocalDate.now().minusDays(1)).city("Vijayawada").zone("Governorpet").estimatedLostHours(new BigDecimal("5.6")).estimatedLostIncome(new BigDecimal("504")).validationStatus("AUTO_APPROVED").claimStatus(Claim.ClaimStatus.APPROVED).fraudFlag(false).payoutAmount(new BigDecimal("504")).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(2)).policy(policies.get(1)).triggerType("ZONE_CLOSURE").disruptionDate(LocalDate.now().minusDays(5)).city("Vijayawada").zone("Governorpet").estimatedLostHours(new BigDecimal("8.0")).estimatedLostIncome(new BigDecimal("720")).validationStatus("PENDING").claimStatus(Claim.ClaimStatus.UNDER_VALIDATION).fraudFlag(true).fraudReason("Suspicious repeated trigger check").payoutAmount(BigDecimal.ZERO).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(3)).policy(policies.get(2)).triggerType("FLOOD_ALERT").disruptionDate(LocalDate.now().minusDays(2)).city("Hyderabad").zone("LB Nagar").estimatedLostHours(new BigDecimal("5.4")).estimatedLostIncome(new BigDecimal("522")).validationStatus("AUTO_APPROVED").claimStatus(Claim.ClaimStatus.APPROVED).fraudFlag(false).payoutAmount(new BigDecimal("522")).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(3)).policy(policies.get(2)).triggerType("HEAVY_RAIN").disruptionDate(LocalDate.now().minusDays(3)).city("Hyderabad").zone("LB Nagar").estimatedLostHours(new BigDecimal("4.2")).estimatedLostIncome(new BigDecimal("406")).validationStatus("PAID").claimStatus(Claim.ClaimStatus.PAID).fraudFlag(false).payoutAmount(new BigDecimal("406")).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(5)).policy(policies.get(4)).triggerType("HEATWAVE").disruptionDate(LocalDate.now().minusDays(3)).city("Guntur").zone("Brodipet").estimatedLostHours(new BigDecimal("4.5")).estimatedLostIncome(new BigDecimal("425")).validationStatus("AUTO_APPROVED").claimStatus(Claim.ClaimStatus.APPROVED).fraudFlag(false).payoutAmount(new BigDecimal("425")).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(5)).policy(policies.get(4)).triggerType("POLLUTION_SPIKE").disruptionDate(LocalDate.now().minusDays(7)).city("Guntur").zone("Brodipet").estimatedLostHours(new BigDecimal("3.6")).estimatedLostIncome(new BigDecimal("340")).validationStatus("REJECTED").claimStatus(Claim.ClaimStatus.REJECTED).fraudFlag(false).payoutAmount(BigDecimal.ZERO).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(1)).policy(policies.get(0)).triggerType("POLLUTION_SPIKE").disruptionDate(LocalDate.now().minusDays(7)).city("Hyderabad").zone("Kukatpally").estimatedLostHours(new BigDecimal("2.8")).estimatedLostIncome(new BigDecimal("260")).validationStatus("AUTO_APPROVED").claimStatus(Claim.ClaimStatus.TRIGGERED).fraudFlag(false).payoutAmount(new BigDecimal("260")).build(),
            Claim.builder().claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()).user(users.get(2)).policy(policies.get(1)).triggerType("FLOOD_ALERT").disruptionDate(LocalDate.now().minusDays(6)).city("Vijayawada").zone("Governorpet").estimatedLostHours(new BigDecimal("7.2")).estimatedLostIncome(new BigDecimal("648")).validationStatus("PAID").claimStatus(Claim.ClaimStatus.PAID).fraudFlag(false).payoutAmount(new BigDecimal("648")).build()
        );
        claimRepository.saveAll(claims);
    }

    private void seedDeliveryActivities(List<User> users) {
        // Seed for workers (users 1-5)
        for (int i = 1; i <= 5; i++) {
            User user = users.get(i);
            for (int day = 0; day < 3; day++) {
                DeliveryActivity activity = DeliveryActivity.builder()
                        .user(user)
                        .date(LocalDate.now().minusDays(day))
                        .loginHours(user.getAverageWorkingHours())
                        .completedDeliveries(15 + (day * 2))
                        .estimatedDailyIncome(user.getAverageDailyEarnings())
                        .activeStatus(true)
                        .sourceType("MOCK")
                        .build();
                deliveryActivityRepository.save(activity);
            }
        }
    }
}
