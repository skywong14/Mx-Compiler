#define bool _Bool
typedef unsigned long size_t;

int printf(const char *pattern, ...);
int sprintf(char *dest, const char *pattern, ...);
int scanf(const char *pattern, ...);
int sscanf(const char *src, const char *pattern, ...);
size_t strlen(const char *str);
int strcmp(const char *s1, const char *s2);
void *memcpy(void *dest, const void *src, size_t n);
void *malloc(size_t n);

void print(char *str) {
    printf("%s", str);
}

void println(char *str) {
    printf("%s\n", str);
}

void printInt(int n) {
    printf("%d", n);
}

void printlnInt(int n) {
    printf("%d\n", n);
}

char *getString() {
    char *buffer = malloc(256);
    scanf("%s", buffer);
    return buffer;
}

int getInt() {
    int n;
    scanf("%d", &n);
    return n;
}

char *toString(int n) {
    char *buffer = malloc(16);
    sprintf(buffer, "%d", n);
    return buffer;
}

int string_length(char *str) {
    return strlen(str);
}

char *string_substring(char *str, int left, int right) {
    int length = right - left;
    char *buffer = malloc(length + 1);
    memcpy(buffer, str + left, length);
    buffer[length] = '\0';
    return buffer;
}

int string_parseInt(char *str) {
    int n;
    sscanf(str, "%d", &n);
    return n;
}

int string_ord(char *str, int pos) {
    return (int)str[pos];
}

char *string_add(char *str1, char *str2) {
    int length1 = strlen(str1);
    int length2 = strlen(str2);
    int length = length1 + length2;
    char *buffer = malloc(length + 1);
    memcpy(buffer, str1, length1);
    memcpy(buffer + length1, str2, length2);
    buffer[length] = '\0';
    return buffer;
}

bool string_equal(char *str1, char *str2) {
    return strcmp(str1, str2) == 0;
}

bool string_notEqual(char *str1, char *str2) {
    return strcmp(str1, str2) != 0;
}

bool string_less(char *str1, char *str2) {
    return strcmp(str1, str2) < 0;
}

bool string_lessOrEqual(char *str1, char *str2) {
    return strcmp(str1, str2) <= 0;
}

bool string_greater(char *str1, char *str2) {
    return strcmp(str1, str2) > 0;
}

bool string_greaterOrEqual(char *str1, char *str2) {
    return strcmp(str1, str2) >= 0;
}

// my code

int array_size(void *arr) {
    // 将指针向前偏移4字节读取数组长度
    return *((int*)((char*)arr - 4));
}


void *_malloc(int n){ //分配4n个字节
    return malloc((size_t)(4 * n));
}


#define NULL (void*)0

void *__arrayDeepCopy__(void *arr) {
    // 获取数组大小
    int size = array_size(arr);

    // 分配新的内存空间存储深拷贝的数组，包括存储size的空间
    int **new_array = malloc(4 * (size + 1)); // +1是为了存储size

    // 将数组大小存储在新数组的头部
    new_array[0] = (int*)size;

    // 返回的指针指向数组的第一个元素
    void **array_ptr = (void **)(new_array + 1);

    for (int i = 0; i < size; i++) {
        // 检查数组元素是否为指针
        if (((void**)arr)[i] != NULL && ((void**)arr)[i] >= (void*)0x1000) {
            // 如果是指针，递归深拷贝
            array_ptr[i] = __arrayDeepCopy__(((void**)arr)[i]);
        } else {
            // 如果不是指针，直接复制值
            array_ptr[i] = ((void**)arr)[i];
        }
    }

    return array_ptr;
}

bool __ptrEqual__(void *ptr1, void *ptr2) {
    return ptr1 == ptr2;
}

bool __ptrNotEqual__(void *ptr1, void *ptr2) {
    return ptr1 != ptr2;
}


/*
void *calloc(size_t num, size_t size);

void *__calloc_class__(size_t class_size) {
    // 使用 calloc 分配并初始化内存
    void *obj = calloc(1, class_size);
    // 返回指向分配内存的指针
    return obj;
}
*/