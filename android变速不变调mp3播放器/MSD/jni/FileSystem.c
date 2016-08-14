#include <unistd.h>  
#include <sys/stat.h>  
#include <sys/time.h>  
#include <sys/types.h>  
#include <stdlib.h>  
#include <fcntl.h>  
#include"FileSystem.h"  
 //文件打开
 int file_open(const char *filename, int flags)
{  
int access;  
    T_pFILE fd = 0;  
    if (flags ==  _CREATE) {  
        access = O_CREAT | O_TRUNC | O_RDWR;  
    } else if (flags == _WRONLY) {  
        access = O_CREAT | O_TRUNC | O_WRONLY;  
    } else if (flags == _RDONLY){  
        access = O_RDONLY;  
    } else if (flags == _RDWR){  
        access = O_RDWR;  
    } else{  
        return -1;  
    }  
      
#ifdef O_BINARY  
    access |= O_BINARY;  
#endif  
    fd = open(filename, access, 0666);  
    if (fd == -1)  
        return -1;  
    return fd;  
}  
//文件读操作
int file_read(T_pFILE fd, unsigned char *buf, int size)  
{  
      
    return read(fd, buf, size);  
}  
//文件写操作
int file_write(T_pFILE fd, unsigned char *buf, int size)  
{  
      
    return write(fd, buf, size);  
}  
  
//文件定位
int64_t file_seek(T_pFILE fd, int64_t pos, int whence)  
{  
      
    if (whence == 0x10000) {  
        struct stat st;  
        int ret = fstat(fd, &st);  
        return ret < 0 ? -1 : st.st_size;  
    }  
    return lseek(fd, pos, whence);  
}  
//关闭文件
int file_close(T_pFILE fd)  
{  
     
    return close(fd);  
}  
//获取文件大小
int file_size(char* filename)
{
    struct stat statbuf;
    stat(filename,&statbuf);
    int size=statbuf.st_size;

    return size;
}
