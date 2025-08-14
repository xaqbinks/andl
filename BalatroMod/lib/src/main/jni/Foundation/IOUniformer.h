#ifndef VIRTUALAPP_IOUNIFORMER_H
#define VIRTUALAPP_IOUNIFORMER_H

#include <jni.h>

namespace IOUniformer {
    void init_env_before_all();

    void redirect(const char *orig_path, const char *new_path);

    const char *query(const char *orig_path);

    void forbid(const char *path);

    void whitelist(const char *path);

    const char *reverse(const char *path);

    void startUniformer(const char *so_path, int api_level, int preview_api_level);
};

#endif //VIRTUALAPP_IOUNIFORMER_H
