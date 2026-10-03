package com.lovely.gweather.data.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CredentialHasherTest {

    @Test
    fun `hash verifies matching password and rejects different password`() {
        val credential = CredentialHasher.hash("correct horse battery staple")

        assertThat(CredentialHasher.verify("correct horse battery staple", credential.hash, credential.salt)).isTrue()
        assertThat(CredentialHasher.verify("different password", credential.hash, credential.salt)).isFalse()
    }

    @Test
    fun `hash uses a unique salt for each credential`() {
        val first = CredentialHasher.hash("same password")
        val second = CredentialHasher.hash("same password")

        assertThat(first.salt).isNotEqualTo(second.salt)
        assertThat(first.hash).isNotEqualTo(second.hash)
    }
}