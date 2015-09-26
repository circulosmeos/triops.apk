// based on bigtest.c 
// @ http://cr.yp.to/snuffle/bigtest.c
//
#include <stdio.h>
// inttypes instead of stdint for Solaris compatibility
#include <inttypes.h>
#include <memory.h>
//#include "hash.c"

unsigned char s[2048];
unsigned char m[2048];
unsigned char c[512/8];

// taken from chacha20-simple-1.0/test.c 
// @ http://chacha20.insanecoding.org/
void hex2byte(const char *hex, uint8_t *byte)
{
  while (*hex) { sscanf(hex, "%2hhx", byte++); hex += 2; }
}

main()
{

  int i;
  int bytes_plaintext;
  //char *plaintext="0000000000000000000000000000000000000000000000000000000000000000";
  //unsigned char *plaintext="616263";
  //unsigned char *plaintext="cc";
  unsigned char *plaintext="3a3a819c48efde2ad914fbf00e18ab6bc4f14513ab27d0c178a188b61431e7f5623cb66b23346775d386b50e982c493adbbfc54b9a3cd383382336a1a0b2150a15358f336d03ae18f666c7573d55c4fd181c29e6ccfde63ea35f0adf5885cfc0a3d84a2b2e4dd24496db789e663170cef74798aa1bbcd4574ea0bba40489d764b2f83aadc66b148b4a0cd95246c127d5871c4f11418690a5ddf01246a0c80a43c70088b6183639dcfda4125bd113a8f49ee23ed306faac576c3fb0c1e256671d817fc2534a52f5b439f72e424de376f4c565cca82307dd9ef76da5b7c4eb7e085172e328807c02d011ffbf33785378d79dc266f6a5be6bb0e4a92eceebaeb1";

  // plaintext
  memcpy(s,plaintext,strlen(plaintext)+1);
  bytes_plaintext=strlen(s)/2;
  hex2byte(s, m); // transforms from left (string) to right (bytes)
  for (i = 0;i < bytes_plaintext;++i) printf("%02x",m[i]); printf("\n"); fflush(stdout);    
  
  crypto_hash(c,m,bytes_plaintext);
  
  for (i = 0;i < 512/8;++i) printf("%02x",c[i]); printf("\n"); fflush(stdout);

  return 0;
}
