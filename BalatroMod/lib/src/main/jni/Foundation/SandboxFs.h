#ifndef VIRTUALAPP_SANDBOXFS_H
#define VIRTUALAPP_SANDBOXFS_H

#include <string>
#include <vector>

enum {
    KEEP,
    FORBID,
    MATCH,
    NOT_MATCH
};

typedef struct {
    char *path;
    size_t size;
    bool is_folder;
} PathItem;

typedef struct {
    char *orig_path;
    size_t orig_size;
    char *new_path;
    size_t new_size;
    bool is_folder;
} ReplaceItem;


int add_keep_item(const char *path);

int add_forbidden_item(const char *path);

int add_replace_item(const char *orig_path, const char *new_path);

const char *relocate_path(const char *path, int *result);

int relocate_path_inplace(char *_path, size_t size, int *result);

const char *reverse_relocate_path(const char *_path);

int reverse_relocate_path_inplace(char *_path, size_t size);

#endif //VIRTUALAPP_SANDBOXFS_H
