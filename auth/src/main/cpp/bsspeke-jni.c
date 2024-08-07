
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>

#include "bsspeke.h"
#include <jni.h>


JNIEXPORT jlong

JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_getClientContext(JNIEnv *env, jobject thiz) {
    bsspeke_client_ctx *ctx;
    ctx = malloc(sizeof(*ctx));
    return (jlong)
            ctx;
}

JNIEXPORT jint

JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_initClient(JNIEnv
                                                      *env,
                                                      jobject thiz, jlong
                                                      client_context,
                                                      jstring client_id_str, jstring
                                                      server_id_str,
                                                      jstring password_str
) {

    const char *client_id = (*env)->GetStringUTFChars(env, client_id_str, NULL);
    size_t client_id_len = (size_t) (*env)->GetStringUTFLength(env, client_id_str);

    const char *server_id = (*env)->GetStringUTFChars(env, server_id_str, NULL);
    size_t server_id_len = (size_t) (*env)->GetStringUTFLength(env, server_id_str);

    const char *password = (*env)->GetStringUTFChars(env, password_str, NULL);
    size_t password_len = (size_t) (*env)->GetStringUTFLength(env, password_str);

    int rc = bsspeke_client_init((bsspeke_client_ctx *) client_context,
                                 client_id, client_id_len, server_id, server_id_len, password,
                                 password_len
    );

    (*env)->ReleaseStringUTFChars(env, client_id_str, client_id);
    (*env)->ReleaseStringUTFChars(env, server_id_str, server_id);
    (*env)->ReleaseStringUTFChars(env, password_str, password);
    return rc;
}

JNIEXPORT void JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_clientBlindSalt(JNIEnv
                                                           *env,
                                                           jobject thiz, jbyteArray
                                                           blind_byte_array,
                                                           jlong client_context
) {
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, blind_byte_array, NULL);

    bsspeke_client_generate_blind((uint8_t
    *) dataPtr, (bsspeke_client_ctx *) client_context);

    (*env)->
            ReleaseByteArrayElements(env, blind_byte_array, dataPtr,
                                     0);
}

JNIEXPORT jint

JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_generatePandV(JNIEnv *env, jobject thiz,
                                                         jbyteArray p_byte_array,
                                                         jbyteArray v_byte_array,
                                                         jbyteArray blind_salt_byte_array,
                                                         jint phf_blocks, jint phf_iterations,
                                                         jlong client_context) {

    jbyte *p = (*env)->GetByteArrayElements(env, p_byte_array, NULL);
    jbyte *v = (*env)->GetByteArrayElements(env, v_byte_array, NULL);
    jbyte *blind_salt = (*env)->GetByteArrayElements(env, blind_salt_byte_array, NULL);

    int rc = bsspeke_client_generate_P_and_V((uint8_t *)
                                                     p, (uint8_t *)
                                                     v, (uint8_t *)
                                                     blind_salt, (uint32_t) phf_blocks,
                                             (uint32_t) phf_iterations,
                                             (bsspeke_client_ctx *) client_context);

    (*env)->ReleaseByteArrayElements(env, p_byte_array, p, 0);
    (*env)->ReleaseByteArrayElements(env, v_byte_array, v, 0);
    (*env)->ReleaseByteArrayElements(env, blind_salt_byte_array, blind_salt, 0);

    return rc;
}

JNIEXPORT jint

JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_generateA(JNIEnv *env, jobject thiz,
                                                     jbyteArray blind_salt_byte_array,
                                                     jint phf_blocks, jint phf_iterations,
                                                     jlong client_context) {

    jbyte *blind_salt = (*env)->GetByteArrayElements(env, blind_salt_byte_array, NULL);

    int rc = bsspeke_client_generate_A((uint8_t *)
                                               blind_salt, (uint32_t) phf_blocks,
                                       (uint32_t) phf_iterations,
                                       (bsspeke_client_ctx *) client_context);

    (*env)->ReleaseByteArrayElements(env, blind_salt_byte_array, blind_salt, 0);

    return rc;
}

JNIEXPORT void JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_clientDeriveSharedKey(JNIEnv
                                                                 *env,
                                                                 jobject thiz, jbyteArray
                                                                 b_byte_array,
                                                                 jlong client_context
) {
    jbyte *b = (*env)->GetByteArrayElements(env, b_byte_array, NULL);

    bsspeke_client_derive_shared_key((uint8_t
    *) b, (bsspeke_client_ctx *) client_context);

    (*env)->
            ReleaseByteArrayElements(env, b_byte_array, b,
                                     0);
}

JNIEXPORT void JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_clientGenerateVerifier(JNIEnv
                                                                  *env,
                                                                  jobject thiz, jbyteArray
                                                                  client_verifier_byte_array,
                                                                  jlong client_context
) {
    jbyte *client_verifier = (*env)->GetByteArrayElements(env, client_verifier_byte_array, NULL);
    bsspeke_client_generate_verifier((uint8_t
    *) client_verifier, (bsspeke_client_ctx *) client_context);
    (*env)->
            ReleaseByteArrayElements(env, client_verifier_byte_array, client_verifier,
                                     0);
}

JNIEXPORT jint

JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_clientVerifyServer(JNIEnv *env, jobject thiz,
                                                              jbyteArray client_verifier_byte_array,
                                                              jlong client_context) {

    jbyte *client_verifier = (*env)->GetByteArrayElements(env, client_verifier_byte_array, NULL);
    int rs = bsspeke_client_verify_server((uint8_t *)
                                                  client_verifier,
                                          (bsspeke_client_ctx *) client_context);
    (*env)->ReleaseByteArrayElements(env, client_verifier_byte_array, client_verifier, 0);
    return rs;
}

JNIEXPORT void JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_clientGetA(JNIEnv
                                                      *env,
                                                      jobject thiz, jbyteArray
                                                      a_byte_array,
                                                      jlong client_context
) {
    jbyte *a = (*env)->GetByteArrayElements(env, a_byte_array, NULL);
    bsspeke_client_get_A((uint8_t *) a,
                         (bsspeke_client_ctx *) client_context);
    (*env)->ReleaseByteArrayElements(env, a_byte_array, a, 0);
}

JNIEXPORT void JNICALL
Java_org_futo_circles_auth_bsspeke_BSSpekeUtils_generateHashedKey(JNIEnv *env, jobject thiz,
                                                             jbyteArray k_byte_array,
                                                             jbyteArray message_byte_array,
                                                             jlong client_context) {
    jbyte *k = (*env)->GetByteArrayElements(env, k_byte_array, NULL);
    jbyte *message = (*env)->GetByteArrayElements(env, message_byte_array, NULL);
    size_t message_len = (size_t) (*env)->GetArrayLength(env, message_byte_array);

    bsspeke_client_generate_hashed_key((uint8_t *) k, (uint8_t *) message, message_len,
                                       (bsspeke_client_ctx *) client_context);

    (*env)->ReleaseByteArrayElements(env, k_byte_array, k, 0);
    (*env)->ReleaseByteArrayElements(env, message_byte_array, message, 0);
}
