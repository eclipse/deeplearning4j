//
// Created by agibsonccc on 3/5/16.
//

#ifndef NATIVEOPERATIONS_POINTERCAST_H
#define NATIVEOPERATIONS_POINTERCAST_H

#include <stdint.h>

typedef void* Nd4jPointer;
typedef long long Nd4jLong;
typedef int Nd4jInt;
typedef int Nd4jStatus;

#define ND4J_STATUS_OK            0
#define ND4J_STATUS_BAD_INPUT     1
#define ND4J_STATUS_BAD_SHAPE     2
#define ND4J_STATUS_BAD_RANK      3
#define ND4J_STATUS_BAD_PARAMS    4
#define ND4J_STATUS_BAD_OUTPUT    5
#define ND4J_STATUS_BAD_RNG       6
#define ND4J_STATUS_BAD_EPSILON   7
#define ND4J_STATUS_BAD_GRADIENTS 8
#define ND4J_STATUS_BAD_BIAS      9

#define ND4J_STATUS_VALIDATION      20

#define ND4J_STATUS_BAD_GRAPH      30
#define ND4J_STATUS_BAD_LENGTH      31
#define ND4J_STATUS_BAD_DIMENSIONS      32
#define ND4J_STATUS_BAD_ORDER      33
#define ND4J_STATUS_BAD_ARGUMENTS      34

#define ND4J_STATUS_DOUBLE_WRITE      40
#define ND4J_STATUS_DOUBLE_READ       45


#define ND4J_STATUS_KERNEL_FAILURE      50


#define ND4J_STATUS_TRUE    100
#define ND4J_STATUS_FALSE   101
#define ND4J_STATUS_MAYBE   119



#endif //NATIVEOPERATIONS_POINTERCAST_H
