

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CossiInput {

    private String facilityId;

    private String planId;

    private Instant startTime;

    private Instant endTime;

    private Set<String> stackingFilters;

    private Set<String> constantAllocationSFs;

    private Map<String, Map<Object, Integer>> volumeProjectionsForShipSorterSfs;

    private Set<String> palletizationChutes;

    private Set<String> chutesConnectingToExtensionSorters;

    private Map<String, Set<String>> chutesOnExtensionSorter;

    private Map<String, Map<Integer, Integer>> chuteProcessingRates;

    private Map<String, Integer> maximumSFsForChute;

    private Map<String, Map<String, Integer>> distanceFromChuteToStagingForSF;

    private Map<String, Set<String>> chuteToRestrictedSFs;

    private Set<String> chutesWhereMirroringIsRequired;

    private Map<String, Map<String, Integer>> freeReallocationCountOfSABelowChuteForSF;

    private Map<String, Map<String, Integer>> currentAllocation;

    private Map<String, Integer> extensionSorterChuteCapacities;

    private Integer sfReallocateFromChuteLimit;

    private Integer sfReallocateFromSALimit;

    private Integer weightOnDistanceMetric;

    private Integer weightOnSfChuteAssignments;

    private Integer avgPalletizeRatePerHourForStacking;

    private Integer stackingAreaMirroringVolThreshold;

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Set<String> $default$stackingFilters() {
        return new HashSet<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Set<String> $default$constantAllocationSFs() {
        return new HashSet<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Map<Object, Integer>> $default$volumeProjectionsForShipSorterSfs() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Set<String> $default$palletizationChutes() {
        return new HashSet<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Set<String> $default$chutesConnectingToExtensionSorters() {
        return new HashSet<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Set<String>> $default$chutesOnExtensionSorter() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Map<Integer, Integer>> $default$chuteProcessingRates() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Integer> $default$maximumSFsForChute() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Map<String, Integer>> $default$distanceFromChuteToStagingForSF() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Set<String>> $default$chuteToRestrictedSFs() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Set<String> $default$chutesWhereMirroringIsRequired() {
        return new HashSet<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Map<String, Integer>> $default$freeReallocationCountOfSABelowChuteForSF() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Map<String, Integer>> $default$currentAllocation() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Map<String, Integer> $default$extensionSorterChuteCapacities() {
        return new HashMap<>();
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Integer $default$sfReallocateFromChuteLimit() {
        return 0;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Integer $default$sfReallocateFromSALimit() {
        return 0;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Integer $default$weightOnDistanceMetric() {
        return 0;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Integer $default$weightOnSfChuteAssignments() {
        return 0;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Integer $default$avgPalletizeRatePerHourForStacking() {
        return 0;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static Integer $default$stackingAreaMirroringVolThreshold() {
        return 0;
    }


    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public static class CossiInputBuilder {
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private String facilityId;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private String planId;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Instant startTime;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Instant endTime;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean stackingFilters$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Set<String> stackingFilters;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean constantAllocationSFs$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Set<String> constantAllocationSFs;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean volumeProjectionsForShipSorterSfs$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Map<Object, Integer>> volumeProjectionsForShipSorterSfs;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean palletizationChutes$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Set<String> palletizationChutes;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean chutesConnectingToExtensionSorters$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Set<String> chutesConnectingToExtensionSorters;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean chutesOnExtensionSorter$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Set<String>> chutesOnExtensionSorter;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean chuteProcessingRates$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Map<Integer, Integer>> chuteProcessingRates;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean maximumSFsForChute$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Integer> maximumSFsForChute;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean distanceFromChuteToStagingForSF$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Map<String, Integer>> distanceFromChuteToStagingForSF;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean chuteToRestrictedSFs$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Set<String>> chuteToRestrictedSFs;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean chutesWhereMirroringIsRequired$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Set<String> chutesWhereMirroringIsRequired;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean freeReallocationCountOfSABelowChuteForSF$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Map<String, Integer>> freeReallocationCountOfSABelowChuteForSF;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean currentAllocation$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Map<String, Integer>> currentAllocation;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean extensionSorterChuteCapacities$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Map<String, Integer> extensionSorterChuteCapacities;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean sfReallocateFromChuteLimit$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Integer sfReallocateFromChuteLimit;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean sfReallocateFromSALimit$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Integer sfReallocateFromSALimit;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean weightOnDistanceMetric$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Integer weightOnDistanceMetric;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean weightOnSfChuteAssignments$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Integer weightOnSfChuteAssignments;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean avgPalletizeRatePerHourForStacking$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Integer avgPalletizeRatePerHourForStacking;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private boolean stackingAreaMirroringVolThreshold$set;
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            private Integer stackingAreaMirroringVolThreshold;

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            CossiInputBuilder() {
        }

        /**
         *
         *  **********
         *  Start commenting out arguments here and in the constructor definition below to
         *  see the call graph construction speed up.
         */
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
            public CossiInput build() {
            return new CossiInput(facilityId,
                                  planId, 
                                   startTime, 
                                   endTime, 
                                   stackingFilters$set ? stackingFilters : CossiInput.$default$stackingFilters(),
                                   constantAllocationSFs$set ? constantAllocationSFs : CossiInput.$default$constantAllocationSFs(),
                                   volumeProjectionsForShipSorterSfs$set ? volumeProjectionsForShipSorterSfs : CossiInput.$default$volumeProjectionsForShipSorterSfs(),
                                   palletizationChutes$set ? palletizationChutes : CossiInput.$default$palletizationChutes(),
                                  chutesConnectingToExtensionSorters$set ? chutesConnectingToExtensionSorters : CossiInput.$default$chutesConnectingToExtensionSorters(), 
                                  chutesOnExtensionSorter$set ? chutesOnExtensionSorter : CossiInput.$default$chutesOnExtensionSorter(), 
                                  chuteProcessingRates$set ? chuteProcessingRates : CossiInput.$default$chuteProcessingRates(), 
                                  maximumSFsForChute$set ? maximumSFsForChute : CossiInput.$default$maximumSFsForChute(), 
                                  distanceFromChuteToStagingForSF$set ? distanceFromChuteToStagingForSF : CossiInput.$default$distanceFromChuteToStagingForSF(), 
                                  chuteToRestrictedSFs$set ? chuteToRestrictedSFs : CossiInput.$default$chuteToRestrictedSFs(), 
                                  chutesWhereMirroringIsRequired$set ? chutesWhereMirroringIsRequired : CossiInput.$default$chutesWhereMirroringIsRequired(), 
                                  freeReallocationCountOfSABelowChuteForSF$set ? freeReallocationCountOfSABelowChuteForSF : CossiInput.$default$freeReallocationCountOfSABelowChuteForSF(), 
                                  currentAllocation$set ? currentAllocation : CossiInput.$default$currentAllocation(), 
                                  extensionSorterChuteCapacities$set ? extensionSorterChuteCapacities : CossiInput.$default$extensionSorterChuteCapacities(), 
                                  sfReallocateFromChuteLimit$set ? sfReallocateFromChuteLimit : CossiInput.$default$sfReallocateFromChuteLimit(), 
                                  sfReallocateFromSALimit$set ? sfReallocateFromSALimit : CossiInput.$default$sfReallocateFromSALimit(), 
                                  weightOnDistanceMetric$set ? weightOnDistanceMetric : CossiInput.$default$weightOnDistanceMetric(), 
                                   weightOnSfChuteAssignments$set ? weightOnSfChuteAssignments : CossiInput.$default$weightOnSfChuteAssignments(), 
                                   avgPalletizeRatePerHourForStacking$set ? avgPalletizeRatePerHourForStacking : CossiInput.$default$avgPalletizeRatePerHourForStacking(), 
                                  stackingAreaMirroringVolThreshold$set ? stackingAreaMirroringVolThreshold : CossiInput.$default$stackingAreaMirroringVolThreshold()
                            );
        }

    }


    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public CossiInput(final String facilityId,
                       final String planId,
                       final Instant startTime, 
                       final Instant endTime, 
                      final Set<String> stackingFilters,
                      final Set<String> constantAllocationSFs,
                      final Map<String, Map<Object, Integer>> volumeProjectionsForShipSorterSfs,
                      final Set<String> palletizationChutes,
                      final Set<String> chutesConnectingToExtensionSorters,
                      final Map<String, Set<String>> chutesOnExtensionSorter, 
                      final Map<String, Map<Integer, Integer>> chuteProcessingRates, 
                      final Map<String, Integer> maximumSFsForChute, 
                      final Map<String, Map<String, Integer>> distanceFromChuteToStagingForSF, 
                      final Map<String, Set<String>> chuteToRestrictedSFs, 
                      final Set<String> chutesWhereMirroringIsRequired, 
                      final Map<String, Map<String, Integer>> freeReallocationCountOfSABelowChuteForSF, 
                      final Map<String, Map<String, Integer>> currentAllocation, 
                      final Map<String, Integer> extensionSorterChuteCapacities, 
                      final Integer sfReallocateFromChuteLimit, 
                      final Integer sfReallocateFromSALimit, 
                      final Integer weightOnDistanceMetric, 
                       final Integer weightOnSfChuteAssignments, 
                       final Integer avgPalletizeRatePerHourForStacking, 
                      final Integer stackingAreaMirroringVolThreshold) {
        this.facilityId = facilityId;
        this.planId = planId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stackingFilters = stackingFilters;
        this.constantAllocationSFs = constantAllocationSFs;
        this.volumeProjectionsForShipSorterSfs = volumeProjectionsForShipSorterSfs;
        this.palletizationChutes = palletizationChutes;
        this.chutesConnectingToExtensionSorters = chutesConnectingToExtensionSorters;
        this.chutesOnExtensionSorter = chutesOnExtensionSorter;
        this.chuteProcessingRates = chuteProcessingRates;
        this.maximumSFsForChute = maximumSFsForChute;
        this.distanceFromChuteToStagingForSF = distanceFromChuteToStagingForSF;
        this.chuteToRestrictedSFs = chuteToRestrictedSFs;
        this.chutesWhereMirroringIsRequired = chutesWhereMirroringIsRequired;
        this.freeReallocationCountOfSABelowChuteForSF = freeReallocationCountOfSABelowChuteForSF;
        this.currentAllocation = currentAllocation;
        this.extensionSorterChuteCapacities = extensionSorterChuteCapacities;
        this.sfReallocateFromChuteLimit = sfReallocateFromChuteLimit;
        this.sfReallocateFromSALimit = sfReallocateFromSALimit;
        this.weightOnDistanceMetric = weightOnDistanceMetric;
        this.weightOnSfChuteAssignments = weightOnSfChuteAssignments;
        this.avgPalletizeRatePerHourForStacking = avgPalletizeRatePerHourForStacking;
        this.stackingAreaMirroringVolThreshold = stackingAreaMirroringVolThreshold;
    }

}
