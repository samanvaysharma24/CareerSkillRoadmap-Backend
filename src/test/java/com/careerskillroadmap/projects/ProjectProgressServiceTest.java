package com.careerskillroadmap.projects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectProgressServiceTest {

    @Test
    void levelsUseCumulativeHundredTimesTriangularThresholdsAndCapAtThirty() {
        assertThat(ProjectProgressService.levelFor(0)).isEqualTo(1);
        assertThat(ProjectProgressService.levelFor(99)).isEqualTo(1);
        assertThat(ProjectProgressService.levelFor(100)).isEqualTo(2);
        assertThat(ProjectProgressService.levelFor(5_499)).isEqualTo(10);
        assertThat(ProjectProgressService.levelFor(5_500)).isEqualTo(11);
        assertThat(ProjectProgressService.levelFor(18_999)).isEqualTo(19);
        assertThat(ProjectProgressService.levelFor(19_000)).isEqualTo(20);
        assertThat(ProjectProgressService.levelFor(20_999)).isEqualTo(20);
        assertThat(ProjectProgressService.levelFor(21_000)).isEqualTo(21);
        assertThat(ProjectProgressService.levelFor(43_499)).isEqualTo(29);
        assertThat(ProjectProgressService.levelFor(43_500)).isEqualTo(30);
        assertThat(ProjectProgressService.levelFor(Long.MAX_VALUE)).isEqualTo(30);
    }
}
