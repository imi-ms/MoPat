package de.imi.mopat.model.enumeration;

import de.imi.mopat.helper.controller.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BodyPart enum contains the information for a selectable region in a Question of
 * QuestionType#BODY_PART. It defines the region in the svg image by it's path and with which image
 * it is associated whith by its ImageType.
 */
public enum BodyPart {
    FRONT_HEAD("bodyPart.front.head", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:round;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25000000000000000;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 67.870646,38.129165 c -1.632744,0.83193 -2.76147,-4.384345 -3.972916,-8.607984 -0.561668,-3.2778 0.481884,-2.943856 1.655382,-2.317534 C 63.61123,18.722075 63.610648,11.089821 68.698337,5.6836858 71.971856,2.370835 75.986687,0.39234548 81.461875,1.0425 c 4.549245,-0.35775829 9.230172,0.6012962 14.219182,4.6411858 5.616283,7.2155702 4.908793,14.0798202 2.64861,20.8578082 1.176188,0.582899 2.387713,1.147333 1.489844,2.814149 -0.832716,7.815278 -2.233082,7.115798 -3.476301,8.773522 -1.321683,7.590314 -4.136417,10.955821 -7.449218,12.911977 -3.824889,2.562376 -7.877954,3.641607 -12.5809,0.496614 -5.212641,-3.456674 -7.214439,-5.233501 -8.442446,-13.408591 z\"\n"
        + "     id=\"bodyPart-front-head\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_THROAT("bodyPart.front.throat", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 65.256941,61.839019 c 5.088919,-0.851929 4.96834,-11.743205 5.28075,-15.1725 3.817662,3.336479 6.791642,6.632832 11.900313,6.526619 7.142801,-0.212648 7.97272,-4.213025 11.125996,-6.819388 0.804179,1.092755 -0.991611,13.039007 5.96925,16.07625 -3.463364,-0.195194 -5.87198,-1.205421 -13.950021,2.165271 -1.924466,0.194955 -2.799271,1.057876 -6.952603,-0.165538 -9.517378,-3.481584 -10.215297,-2.411189 -13.373685,-2.610714 z\"\n"
        + "     id=\"bodyPart-front-throat\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_SHOULDER("bodyPart.front.rightShoulder", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 44.673375,68.955 c 4.737607,9.892049 4.656162,19.878588 -1.8575,29.99125 0.878204,-0.111107 1.646621,1.56371 2.541581,2.41849 l -4.138454,28.88641 c -4.119813,-4.38194 -8.406655,-8.39503 -17.712583,-1.3243 0.09892,-7.76444 0.272713,-15.51225 2.317534,-22.84427 1.198418,-0.40063 -3.86272,-17.415724 0.993229,-25.823953 2.890607,-8.52927 14.194312,-8.671549 17.856193,-11.303627 z\"\n"
        + "     id=\"bodyPart-front-rightShoulder\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_SHOULDER("bodyPart.front.leftShoulder", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 119.28913,69.64875 c 6.61795,1.572671 13.64414,1.240182 18.48243,11.117922 3.73416,7.356237 0.48036,19.562748 0.70232,26.219908 2.64974,9.49526 2.9983,16.48015 2.22401,22.2401 -6.64578,-5.95988 -12.47282,-5.04239 -17.55797,2.10696 l -4.13305,-29.56739 2.02609,-2.856339 C 114.62554,85.660505 116.472,77.363429 119.28913,69.64875 Z\"\n"
        + "     id=\"bodyPart-front-leftShoulder\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_LOWER_ARM("bodyPart.front.leftLowerArm", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 124.94438,145.49 c 2.14513,5.15944 11.13558,5.54852 17.15815,-0.34409 2.59168,7.43558 5.14117,14.85428 4.68213,21.06957 1.12993,4.69265 0.51973,15.49799 3.27749,20.60135 -2.41822,-3.8467 -7.60916,-5.3321 -11.70532,1.87285 l -9.59836,-24.11295 z\"\n"
        + "     id=\"bodyPart-front-leftLowerArm\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_LOWER_ARM("bodyPart.front.rightLowerArm", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 22.2401,144.09243 c 3.949999,2.29367 9.43952,8.41391 16.1129,1.81882 0.102011,19.61289 -10.053773,33.51065 -12.363509,42.52657 -4.958124,-5.76176 -8.18681,-4.60576 -11.422133,-3.47631 2.110956,-5.11499 2.565697,-10.89247 2.483073,-16.88489 0.09336,-5.94907 1.568661,-13.62556 5.189669,-23.98419 z\"\n"
        + "     id=\"bodyPart-front-rightLowerArm\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_HAND("bodyPart.front.leftHand", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 138.35683,188.68968 c 3.11718,-5.20108 6.72519,-7.53897 11.70532,-1.87285 2.02253,4.05662 7.56678,8.42725 8.83035,12.64942 l 3.98697,4.44034 c 0.62769,0.62781 2.40048,5.12266 -2.39959,2.86781 l -3.65836,-3.40261 c 0.39913,1.50019 -0.0945,5.33636 0.14676,7.73357 1.35543,3.59429 0.95887,5.55174 1.52169,8.25225 0.416,6.22459 -1.87285,4.097 -2.80927,0.70232 l -1.28759,-7.60846 c -0.74412,-2.14032 -1.50253,-4.45221 -1.63874,0.70232 l 0.93642,8.66194 c -0.69515,1.90628 -0.72365,6.92363 -3.16043,0.70231 l -1.52169,-10.30067 c -1.06035,-2.86142 -1.13237,-1.06385 -1.28759,0.46821 l 0.93643,8.89604 c 0.13986,3.87697 -2.85988,5.00569 -3.5116,0.58526 l -0.58526,-10.65183 c -0.71006,-1.19221 -1.43104,-2.69003 -1.98991,0.35116 l -0.35116,6.55497 c -2.71461,6.51448 -2.9639,0.36427 -2.92632,-1.05347 0.1309,-4.32173 0.31974,-8.60485 0,-13.22701 l 0.2341,-11.35416 z\"\n"
        + "     id=\"bodyPart-front-leftHand\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccccccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_HAND("bodyPart.front.rightHand", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 14.567358,184.96151 c 3.371995,-1.5406 6.956375,-1.76442 11.422133,3.47631 -1.525395,2.9511 -1.451872,6.10206 -1.241536,9.27013 0.327406,5.1918 -0.01482,7.92827 -0.331076,10.75998 l 0.331076,9.43568 c -0.142856,2.71782 -0.07553,6.1235 -2.565841,1.15876 l -0.744922,-7.53198 c -0.688231,-2.00832 -1.44428,-1.58667 -1.903689,0.41384 l -0.662153,11.09106 c -1.215143,2.92805 -2.316332,2.89327 -3.310763,0.0828 l 0.744922,-10.51167 c -0.01666,-2.08112 -0.166035,-3.28383 -0.993229,0 l -2.151996,11.91874 c -1.048404,1.87606 -1.876104,1.65542 -2.483073,-0.66215 l 0.91046,-11.67044 c -0.178552,-1.97092 -0.757566,-1.26267 -1.407074,-0.0828 l -2.0692271,10.26337 c -1.8030778,2.35895 -2.1121582,0.4849 -2.6486106,-0.74492 l 1.5726125,-9.84952 0.034941,-7.9762 c -1.1893553,1.27194 -2.0781499,2.7693 -4.0906256,3.4239 -3.3606876,0.98334 -2.26163487,-0.91904 -2.06922702,-2.23477 1.92663762,-2.16128 3.85151102,-4.32608 5.13168302,-7.78029 l 6.4559878,-7.20091 z\"\n"
        + "     id=\"bodyPart-front-rightHand\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccccccccccccccccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_CHEST("bodyPart.front.chest", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 44.673375,68.955 c 6.274326,-0.323952 14.00192,-6.146077 20.583566,-7.115981 4.645814,-0.445194 9.036881,0.892838 13.373685,2.610714 1.989245,0.875903 4.339429,0.849457 6.952603,0.165538 4.795357,-2.029911 9.466506,-2.941946 13.950021,-2.165271 5.20494,-0.01253 12.60223,4.976646 19.75588,7.19875 -4.31201,10.259922 -2.98333,19.936325 1.74383,29.261161 l -6.55497,12.641739 c -8.58486,10.34561 -24.886907,6.47616 -25.062488,4.18359 -7.776064,-5.39878 -16.142801,-0.2336 -16.043257,-0.41769 -6.149317,3.40589 -12.983753,5.87208 -22.571175,-1.77588 -4.234286,-4.614 -4.68781,-13.16662 -7.985195,-14.59531 6.491597,-10.160206 6.598162,-20.143051 1.8575,-29.99125 z\"\n"
        + "     id=\"bodyPart-front-chest\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_STOMACH("bodyPart.front.stomach", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 48.13325,173.76875 c 22.186858,7.37133 44.575921,9.133 67.35938,0.3 -0.52855,-6.45292 -0.58784,-12.74942 -2.88749,-19.7927 -0.21193,-2.5078 -0.17761,-6.00062 0.2341,-11.00299 1.45834,-6.02805 2.67678,-13.01568 3.5116,-21.53778 1.002,-4.51416 3.51021,-9.53039 2.83816,-13.48653 0.74586,-2.9674 0.29277,-4.73585 -0.18213,-6.4825 -1.03883,7.0508 -9.6227,20.72648 -28.011149,15.09103 -6.797059,-5.9099 -16.33589,-2.9917 -17.954552,-1.20877 -7.323264,5.00759 -24.660689,5.61829 -27.683713,-14.28377 L 44.085,108.45875 c 3.314978,8.40532 4.458767,16.81063 5.311432,25.21595 1.111219,8.10632 4.406228,15.12075 0.936425,25.51759 -0.731632,4.85097 0.405189,0.35967 -2.199607,14.57646 z\"\n"
        + "     id=\"bodyPart-front-stomach\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_HIPS("bodyPart.front.hips", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 48.13325,173.76875 c 19.368893,6.04817 39.400265,10.81869 67.35938,0.3 5.01122,6.64057 2.94722,19.47195 3.43338,30.07195 -16.27618,2.12207 -34.084615,4.8684 -34.07201,0.3543 6.548111,-2.74045 9.296183,-6.74758 12.204625,-10.70125 -8.273954,-3.98102 -18.079468,-4.68013 -30.714375,0.68375 3.78441,3.61852 7.158627,7.44214 12.109375,10.4775 -8.581639,4.27781 -18.901575,-0.0713 -33.271107,-2.21894 0.203878,-8.98746 -1.294194,-16.5161 2.950732,-28.96731 z\"\n"
        + "     id=\"bodyPart-front-hips\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_THIGH("bodyPart.front.leftThigh", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 84.854,204.495 c 2.215779,5.17438 21.08248,0.70875 34.07201,-0.3543 1.85245,17.63601 -0.4809,35.27202 -4.21392,52.90803 l -2.57517,14.51459 c -6.2991,-3.71121 -12.567026,-7.48476 -24.347053,-0.23411 -0.428439,-9.3796 -0.300744,-15.70047 -1.872851,-31.37025 C 84.381823,226.95679 82.743539,213.85152 84.854,204.495 Z\"\n"
        + "     id=\"bodyPart-front-leftThigh\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_KNEE("bodyPart.front.leftKnee", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 87.789867,271.32921 c 9.034506,-5.6646 17.077673,-5.13332 24.347053,0.23411 -1.54136,3.49704 -0.0454,15.49862 0.42903,24.63984 -7.05594,7.58244 -14.122467,3.17803 -21.6855,1.15876 -0.0086,-8.20265 -3.963358,-15.37986 -3.090583,-26.03271 z\"\n"
        + "     id=\"bodyPart-front-leftKnee\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_LOWER_LEGG("bodyPart.front.leftLowerLeg", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 90.88045,297.36192 c 8.258661,3.5627 16.04935,5.33149 21.6855,-1.15876 4.69111,21.2758 -1.72078,39.90803 -5.79384,59.09712 l -0.49661,6.29045 c -2.2784,2.41076 -3.72539,5.20522 -13.243054,-0.99323 1.522117,-6.97551 1.18143,-15.81382 -0.82769,-26.32057 -2.7908,-12.06545 -3.634807,-21.66523 -1.579949,-29.76605 0.632059,-0.16802 0.505962,-3.45143 0.255643,-7.14896 z\"\n"
        + "     id=\"bodyPart-front-leftLowerLeg\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     inkscape:label=\"bodyPart-front-leftLowerLeg\"\n"
        + "     sodipodi:nodetypes=\"cccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_FOOT("bodyPart.front.leftFoot", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 93.032446,360.5975 c 6.009397,4.11931 10.758454,5.24532 13.243054,0.99323 1.24608,4.53668 2.12315,8.75707 0.32163,10.68152 2.78532,2.75633 3.13752,5.79461 12.42481,16.96335 0.0831,1.70409 -0.16526,2.99384 -1.32431,3.14523 -0.78308,1.16113 -1.71894,1.55834 -2.81415,1.15877 -0.92492,1.21129 -1.93492,2.08226 -3.4763,0.82769 -1.07275,1.3803 -2.44145,1.42881 -4.13845,0 -0.45076,2.12757 -1.83447,3.38883 -6.62153,1.48984 -2.810651,-3.48364 -4.321453,-7.12975 -4.635067,-10.92552 -4.796532,-5.19595 -2.782781,-8.88947 -2.979687,-13.57413 -2.029311,-1.92807 -0.189015,-7.00793 0,-10.75998 z\"\n"
        + "     id=\"bodyPart-front-leftFoot\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_THIGH("bodyPart.front.rightThigh", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 45.182518,202.73606 c 11.557054,1.51746 25.097553,6.34065 33.271107,2.21894 3.358494,11.9293 1.70715,20.72744 0.206096,29.61952 l -1.638744,18.26029 -0.936426,19.19672 C 64.179704,264.70564 58.759,268.90711 52.205708,271.0951 L 45.650731,235.04273 C 44.127335,221.99497 44.953719,212.8635 45.182518,202.73606 Z\"\n"
        + "     id=\"bodyPart-front-rightThigh\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_KNEE("bodyPart.front.rightKnee", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 52.205708,271.0951 c 9.984351,-4.94981 17.627215,-3.44571 23.878843,0.93643 1.268022,12.01633 -3.546912,21.3678 -3.277488,27.85865 -17.068706,5.80136 -19.304128,2.94997 -21.771887,0.23411 1.011322,-9.36582 2.536287,-18.47483 1.170532,-29.02919 z\"\n"
        + "     id=\"bodyPart-front-rightKnee\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_LOWER_LEG("bodyPart.front.rightLowerLeg", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 51.035176,300.12429 c 5.925309,6.5819 14.223445,1.29966 21.771887,-0.23411 4.248872,14.77578 0.547941,25.84164 -1.460116,37.69752 -1.972289,13.66375 -0.665803,14.86816 -0.662152,21.02334 -4.198373,2.12291 -9.819037,6.26949 -12.911977,1.32431 -0.307842,-5.96808 -3.424164,-17.55313 -5.297221,-26.65164 -2.333852,-9.02248 -3.498929,-19.3253 -1.440421,-33.15942 z\"\n"
        + "     id=\"bodyPart-front-rightLowerLeg\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     inkscape:label=\"#bodyPart.front.rightLowerLeg\"\n"
        + "     sodipodi:nodetypes=\"ccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_FOOT("bodyPart.front.rightFoot", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 57.772818,359.93535 c 4.664812,5.69249 8.638886,-0.35755 12.911977,-1.32431 0.721115,5.11792 3.100878,12.26307 0.165538,12.91198 0.401385,4.51269 1.801762,9.32509 -1.986458,12.5809 l -1.489844,5.46276 -3.807377,6.62153 c -3.206268,1.17871 -6.186114,1.94232 -6.787065,-1.65538 -1.379485,0.63066 -2.758969,2.11454 -4.138454,-0.16554 -0.93805,0.95296 -1.876099,1.18616 -2.814149,-0.49662 -1.755831,0.32117 -2.822111,-0.0472 -2.814148,-1.48984 -2.075919,-0.18911 -1.695467,-1.85205 -1.655382,-3.31076 l 6.787065,-8.11137 4.660604,-8.9487 z\"\n"
        + "     id=\"bodyPart-front-rightFoot\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccc\" class='shape' />",
        Constants.BODY_FRONT), FRONT_RIGHT_ELBOW_JOINT("bodyPart.front.rightElbowJoint", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 23.506419,128.92685 c 6.367184,-5.36501 12.281827,-5.171 17.712583,1.3243 L 38.353,145.91125 c -2.907575,3.53447 -9.048558,5.27286 -16.1129,-1.81882 1.072005,-5.05519 1.090367,-10.11039 1.266319,-15.16558 z\"\n"
        + "     id=\"bodyPart-front-rightElbowJoint\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccccc\"\n"
        + "     inkscape:label=\"#bodyPart.front.rightElbowJoint\" class='shape' />",
        Constants.BODY_FRONT), FRONT_LEFT_ELBOW_JOINT("bodyPart.front.leftElbowJoint", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 123.13992,131.33364 c 5.68095,-8.08554 11.56556,-7.41382 17.55797,-2.10696 -0.15381,10.90463 0.64937,13.19634 1.40464,15.91923 -6.22454,5.67528 -14.32365,5.74196 -17.15815,0.34409 1.31886,-4.71879 -0.85245,-9.43757 -1.80446,-14.15636 z\"\n"
        + "     id=\"bodyPart-front-leftElbowJoint\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccccc\"\n"
        + "     inkscape:label=\"#bodyPart.front.leftElbowJoint\" class='shape' />",
        Constants.BODY_FRONT), FRONT_GENITALS("bodyPart.front.genitals", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.5;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0.25, 0.25;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 78.453625,204.955 c -3.249895,-1.52609 -7.632303,-5.88347 -12.109375,-10.4775 17.373393,-5.93613 20.964114,-3.93633 30.714375,-0.68375 -1.525689,2.29582 2.05872,2.0366 -12.204625,10.70125 z\"\n"
        + "     id=\"bodyPart-front-genitals\"\n" + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccc\" class='shape' />",
        Constants.BODY_FRONT), BACK_LOWER_BACK("bodyPart.back.lowerBack", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 49.068004,133.85927 c 19.109925,6.74541 27.820416,5.48449 31.3142,5.98252 4.61768,0.21469 19.263876,0.23411 35.618556,-5.93143 -1.98772,12.84779 -3.09863,22.4419 -2.72654,25.06385 0.75358,5.76735 1.57384,7.47019 2.1071,9.88912 l 0.29776,4.76413 c -4.91901,3.20936 -26.293512,4.84551 -34.445794,5.18085 -10.404159,-0.6992 -26.066633,-1.00282 -31.954294,-4.73421 0.459197,-7.84182 2.290015,-9.81093 3.126461,-17.56774 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-lowerBack\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"cccccccccc\" />",
        Constants.BODY_BACK), BACK_UPPER_BACK("bodyPart.back.upperBack", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:0\"\n"
        + "     d=\"m 48.531057,70.965611 17.757376,-7.657587 c 10.806143,-1.434606 21.612286,-1.237371 32.418429,-0.297758 l 20.545318,8.039473 c 7.41931,6.611944 1.31072,25.78494 3.05202,32.753411 -0.59662,5.1439 -1.45207,8.21712 -2.44482,10.19191 -3.62126,15.97865 -2.57646,13.29305 -3.85862,19.9153 -10.3101,3.06349 -20.563837,6.19039 -33.96836,5.77266 -14.052007,-0.19046 -24.457645,-2.46457 -32.964396,-5.82375 -0.367008,-7.44189 -2.086265,-13.53153 -4.432065,-18.99463 l -1.158008,-11.8959 C 45.360885,94.237104 40.26409,77.606399 48.531057,70.965611 Z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-upperBack\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccc\" />", Constants.BODY_BACK), BACK_HEAD(
        "bodyPart.back.head", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0,11;stroke-dashoffset:0;stroke-opacity:1\"\n"
        + "     d=\"m 68.633278,41.571672 c 8.364394,3.666978 17.205729,5.23541 27.542639,0.223318 l 0.446638,-2.828703 1.786549,0.07444 1.191033,-2.456505 0.372198,-3.870857 1.339915,-4.764133 c 0.27917,-1.522316 -0.78245,-1.167533 -1.414354,-1.414352 l 1.042154,-4.243056 C 100.60494,18.369683 101.69823,16.21877 96.696994,6.5106368 93.596395,3.7062217 90.60617,0.84661978 82.479037,0.55547167 76.903508,0.77629099 71.807711,2.4363083 67.814443,7.4039116 63.090984,16.320359 65.533264,19.217586 65.283498,24.376132 l 0.669956,3.573099 c -0.744396,-0.197142 -1.488791,-0.849636 -2.233187,0.446638 0.861275,3.975152 1.804302,10.327066 1.935429,9.974901 0.281763,0.95388 1.802757,2.325036 2.456506,0.818835 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-head\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccccccc\" />", Constants.BODY_BACK), BACK_HIPS(
        "bodyPart.back.hips", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:7\"\n"
        + "     d=\"m 49.278992,174.0741 c 6.578089,3.90256 20.598606,3.83582 32.753408,4.76413 18.63344,-0.50888 26.90594,-2.74458 33.79556,-5.21077 l 0.29776,4.46637 2.97758,11.76145 0.7444,14.44128 c -2.34015,0.1948 -6.05348,2.67825 -9.52827,4.76413 -10.426884,7.03168 -18.445285,6.03509 -25.309447,1.19103 -2.482692,-2.58085 -2.324588,-4.63353 -2.977583,-6.84844 -0.597804,4.64429 -1.714334,4.62005 -2.679824,5.95517 -1.170391,1.64814 -13.887881,8.62614 -25.309452,0.7444 -6.987485,-5.15272 -6.631118,-4.0107 -8.486111,-4.76414 l 0.595517,-15.48343 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-hips\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccc\" />",
        Constants.BODY_BACK), BACK_LEFT_ELBOW_JOINT("bodyPart.back.leftElbowJoint", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:0\"\n"
        + "     d=\"m 23.581251,140.13026 c 6.862129,-2.3595 14.13361,-5.51496 16.106836,1.63174 -0.526367,6.76173 -1.052734,10.38797 -1.579101,15.10674 -6.76449,4.6752 -11.9395,0.60826 -17.054298,-3.78984 1.165782,-4.04655 2.927674,-7.59634 2.526563,-12.94864 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftElbowJoint\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccccc\" />",
        Constants.BODY_BACK), BACK_LEFT_FOOT("bodyPart.back.leftFoot", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:0\"\n"
        + "     d=\"m 53.163088,395.62891 c 2.507201,-0.21346 4.05012,-1.96976 5.158399,-4.42149 l 2.526562,-8.84297 c 1.579984,-1.78876 3.545196,-3.1923 5.684766,-4.42148 l 3.36875,2.00019 c 0.12818,1.21601 0.02569,2.20134 0.947461,4.21094 l -2.421289,9.68516 c -0.180984,2.46007 -0.230687,4.83261 0.421094,6.7375 1.120254,1.78166 1.85575,2.40905 3.474023,5.68476 l 0.842188,3.05293 v 5.1584 l -1.368555,1.89492 1.158008,4.42149 -1.158008,1.5791 h -1.789648 l -1.473829,-1.68438 -2.316015,0.63164 -6.10586,-2.10546 -1.579101,-0.73692 -0.736915,-1.89492 -2.210742,-1.89492 -1.473828,-4.63203 -0.947461,-8.84297 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftFoot\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccccccccccccc\" />",
        Constants.BODY_BACK), BACK_LEFT_HAND("bodyPart.back.leftHand", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:7;stroke-opacity:1\"\n"
        + "     d=\"m 25.639585,195.83902 -0.732333,3.80021 -0.4627,9.72212 v 10.64803 l 0.4627,4.62958 -0.69405,3.00921 h -1.8508 c 0.136691,-9.58462 -0.891517,-12.52891 -1.9956,-10.63112 -0.635159,1.09176 -0.217809,10.64713 -2.631408,15.72366 -3.32195,-1.06554 0.949741,-15.33801 -1.478825,-15.5369 -2.087675,-1.2999 -1.231717,17.6693 -4.999,15.07395 0.0586,-5.42514 1.799666,-15.65985 -0.200784,-15.19866 -2.7663831,7.24886 -1.2644921,15.6737 -5.5829751,13.11535 l 0.69405,-9.25917 1.619458,-11.11097 -4.164316,4.62957 -2.3134999,-0.46296 -0.23135,-2.31479 4.8583579,-8.33323 4.6270091,-5.32402 3.284333,-5.96587 c 4.708508,-1.99589 12.350383,3.39679 11.791733,3.78601 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftHand\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccscccccccccccccc\" />"
        + "   sodipodi:nodetypes=\"ccccccccccccccccccc\" />", Constants.BODY_BACK), BACK_LEFT_KNEE(
        "bodyPart.back.leftKnee", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:0\"\n"
        + "     d=\"m 52.847268,284.2496 c 14.612734,-7.52432 19.211188,-5.03436 23.370704,-2.10546 l 0.210547,10.94843 -1.894922,8.00078 0.210547,7.57969 c -4.196038,-7.68509 -11.524825,-7.96549 -21.896876,-1.05273 l 0.842187,-7.79024 -0.421094,-3.78984 0.421094,-7.15859 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftKnee\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"cccccccccc\" />",
        Constants.BODY_BACK), BACK_LEFT_LOWER_ARM("bodyPart.back.leftLowerArm", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 21.054688,153.0789 c 3.55803,3.54081 10.650928,9.39123 17.054298,3.78984 -1.872676,16.20335 -4.291695,17.10912 -6.526954,23.16016 -3.061957,5.91872 -4.862086,11.08035 -5.942447,15.81012 -6.381488,-5.36597 -8.985696,-3.52632 -11.791733,-3.78601 1.558255,-3.73328 2.998301,-7.85073 2.995899,-16.65614 0.605607,-5.68364 -0.101964,-8.47828 4.210937,-22.31797 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftLowerArm\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccccccc\" />",
        Constants.BODY_BACK), BACK_LEFT_LOWER_LEG("bodyPart.back.leftLowerLeg", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 52.847268,308.25195 c 0.676042,-3.0306 18.542458,-11.47475 22.317969,1.68437 1.187898,21.22623 -1.59007,23.41631 -2.947656,32.42422 -2.198081,10.29148 -3.050572,17.89177 -3.579297,24.84454 v 5.26367 l 1.052734,2.52656 -1.052734,3.78984 -2.316016,-0.84218 -5.474219,4.42148 -2.737109,9.05352 c -1.379304,2.69411 -3.113284,3.96952 -5.053125,4.42148 l 2.526562,-13.475 -0.842187,-1.89492 0.63164,-1.89492 C 54.355722,361.30143 53.509589,346.35698 50.952346,328.675 Z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftLowerLeg\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccccc\" />",
        Constants.BODY_BACK), BACK_LEFT_SHOULDER("bodyPart.back.leftShoulder", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 48.531057,70.965611 c -8.830659,7.505723 -2.897716,22.591277 -5.053126,32.003129 0.386003,4.65263 0.772005,8.35234 1.158008,11.8959 l -2.210742,13.58027 -2.73711,13.31709 c -1.194679,-6.39514 -8.236048,-4.98631 -16.106836,-1.63174 l -0.105274,-15.79101 1.789649,-13.26446 c -0.08633,-7.50416 -2.300779,-12.454586 0.105273,-22.949608 1.456523,-4.786031 3.868882,-10.737117 9.685157,-13.15918 2.833892,-1.807114 8.896804,-2.69165 13.475001,-4.000391 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftShoulder\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccccccccc\" />", Constants.BODY_BACK), BACK_LEFT_THIGH(
        "bodyPart.back.leftThigh", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 45.408134,205.33871 c 2.066408,0.40169 5.187345,1.98972 10.421539,5.95517 11.357264,5.76546 20.086006,0.90677 23.671782,-1.78655 2.941639,23.8239 -0.604499,30.80076 -1.488791,40.49512 -1.865099,14.74745 -0.334946,27.79244 -2.269312,31.80467 -5.571606,-4.63969 -16.297118,-1.99451 -22.819013,2.53714 0.177217,-9.34718 -6.34501,-25.19489 -8.2606,-47.44317 -0.414027,-10.55149 1.159885,-17.02149 0.744395,-31.56238 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-leftThigh\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"cccccccc\" />",
        Constants.BODY_BACK), BACK_NECK("bodyPart.back.neck", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 68.633278,41.571672 c 8.586171,3.897568 17.572737,5.221165 27.542639,0.223318 -0.698767,6.179377 -1.488545,8.742519 -1.352695,10.965663 0.507819,8.310295 2.936515,8.995817 3.88364,10.249613 -1.009922,-1.42111 -31.279985,-1.408478 -32.418429,0.297758 1.144445,-1.570482 4.356962,-3.563985 4.151693,-10.050865 -0.08977,-2.837025 -0.788693,-8.232539 -1.806848,-11.685487 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-neck\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccsccsc\" />",
        Constants.BODY_BACK), BACK_RIGHT_ELBOW_JOINT("bodyPart.back.rightElbowJoint", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:7\"\n"
        + "     d=\"m 125.17012,141.70936 c 3.6926,-8.41885 10.33764,-2.81355 16.10684,-1.36855 l 2.94766,13.05391 c -6.32153,4.07189 -12.61057,7.76457 -18.10704,2.21074 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightElbowJoint\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccccc\" />",
        Constants.BODY_BACK), BACK_RIGHT_FOOT("bodyPart.back.rightFoot", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:7\"\n"
        + "     d=\"m 92.640629,381.62754 c 0.98302,-2.66552 2.709816,-3.09972 4.210937,-4.21094 l 4.526754,4.21094 3.26348,4.52676 v 3.89511 c 1.69568,3.80773 3.21669,5.17008 4.63203,5.05313 l 0.73692,15.79101 -0.4211,3.26348 -2.94765,1.5791 -0.21055,2.21074 c -2.20898,1.02118 -4.47943,1.91941 -6.42168,3.47403 l -3.05293,-0.4211 -0.631641,0.94747 -1.158008,0.63164 c -2.341548,0.27595 -1.993784,-0.79276 -2.842383,-1.26329 0.168124,-1.80431 0.49877,-2.44139 0.947461,-3.68456 l -1.473828,-2.10547 c -0.757053,-1.45542 -0.201115,-3.3485 -0.210547,-5.05313 l 2.842383,-5.68477 v -13.68554 c -0.983461,-3.11521 -3.116692,-6.10267 -1.789648,-9.47461 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightFoot\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccccccccccccccccccc\" />",
        Constants.BODY_BACK), BACK_RIGHT_HAND("bodyPart.back.rightHand", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:0\"\n"
        + "     d=\"m 139.35086,195.81045 c 4.1417,-4.9954 7.66316,-5.02893 11.01706,-3.72198 l 7.89059,11.01706 5.21077,8.03947 0.44664,2.38207 c -1.24543,0.57679 -2.55795,0.97747 -5.35965,-2.53095 -1.0938,-1.12986 -1.72633,-2.01695 -2.06503,-1.76543 -0.34729,0.2004 -0.61576,0.37533 -0.76367,1.76543 l 4.61525,15.63231 c -0.10791,1.01984 -0.008,2.17845 -0.66995,2.8287 -0.7469,-0.0945 -1.44703,0.18526 -2.38207,-1.41435 l -1.86099,-9.45382 c -1.77423,-3.00128 -1.29365,-1.49294 -1.83586,-2.03018 -0.14036,0.0378 -0.28073,1.78115 -0.42109,2.89502 l 1.43812,7.24907 -0.29776,3.27534 c -0.59552,1.60996 -1.19103,0.60303 -1.78655,0.14888 l -1.33991,-2.8287 c -0.14888,-3.63594 -0.29776,-6.2123 -0.44664,-9.30495 -0.69096,-1.9164 -0.86878,-1.26707 -1.19103,-1.33991 -0.27142,-0.0557 -0.72467,0.61586 -1.19104,1.33991 0.53378,4.81435 0.0993,7.69209 0.14888,11.53813 -0.16828,1.06382 -0.37913,1.95732 -0.74439,2.23319 -0.2942,0.34388 -0.87388,-0.0259 -1.56324,-0.66996 -0.1985,-0.70009 -0.39701,-1.76073 -0.59551,-3.64754 l 0.0744,-9.00718 c -1.19848,-2.56383 -0.89694,-1.00262 -1.33991,-1.48879 -0.29776,0.34038 -0.59552,0.30357 -0.89328,1.56323 -0.2317,3.74776 -0.25853,5.65169 -0.29776,7.66727 -0.71375,2.3872 -0.83194,1.49887 -1.19103,1.93543 -0.6697,0.024 -1.16343,-0.47981 -1.63767,-1.04215 -0.37304,-1.56407 -0.30552,-2.68759 -0.29776,-3.87086 0.4707,-7.37666 0.62042,-15.15454 0.29776,-23.5229 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightHand\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccccccccccccccccccccccc\" />",
        Constants.BODY_BACK), BACK_RIGHT_KNEE("bodyPart.back.rightKnee", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:0,7.7;stroke-dashoffset:0\"\n"
        + "     d=\"m 88.640238,283.40742 c 7.581988,-6.13501 16.212182,-4.23376 25.476172,2.52656 -1.5344,17.51226 -1.12225,18.8033 -1.05273,22.94961 -7.54296,-5.22763 -14.91344,-6.6607 -21.89688,0.42109 -0.529047,-3.48743 -0.252629,-2.94752 -2.316015,-14.10664 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightKnee\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"cccccc\" />",
        Constants.BODY_BACK), BACK_RIGHT_LOWER_ARM("bodyPart.back.rightLowerArm", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:none\"\n"
        + "     d=\"m 126.2495,155.61308 c 5.23143,5.59138 11.61985,1.92688 18.01438,-1.78654 2.49944,5.85591 2.47891,11.71182 3.12646,17.56773 0.85692,6.91744 0.37714,14.02583 2.97758,20.6942 -6.01876,-2.16842 -9.47031,1.95054 -11.01706,3.72198 l -7.29507,-17.8655 c -2.06966,-5.4305 -4.12544,-11.06925 -5.80629,-22.33187 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightLowerArm\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccccccc\" />",
        Constants.BODY_BACK), BACK_RIGHT_SHOULDER("bodyPart.back.rightShoulder", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 119.25218,71.049739 c 7.47384,6.314253 1.29596,21.85504 3.05202,32.753411 -0.15039,4.84045 -2.83481,12.41423 -1.78655,12.50584 0.40878,7.74557 1.69851,10.2054 2.60539,14.96236 l 2.23319,9.82602 c 2.69897,-6.78816 9.33142,-3.74261 15.85562,-0.96771 l 0.22332,-17.04667 c -0.51367,-3.95634 -1.31786,-6.48811 -2.00987,-12.4314 -0.40976,-7.36142 1.43899,-12.192153 0.59552,-19.800927 -0.66464,-6.164373 -3.16435,-11.374515 -8.78387,-14.962352 -4.42813,-2.623676 -8.1148,-3.517278 -11.98477,-4.838572 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightShoulder\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"ccccccccccc\" />", Constants.BODY_BACK), BACK_RIGHT_THIGH(
        "bodyPart.back.rightThigh", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.69999999;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\"\n"
        + "     d=\"m 84.861103,210.4006 c 6.419515,3.84923 13.560165,5.69531 23.522907,-0.29775 6.22118,-3.8196 9.70644,-5.35925 11.16593,-5.21077 1.75876,35.35453 -0.24194,28.72844 -0.59552,40.49512 -2.03427,11.19261 -5.28208,32.72078 -4.97468,40.18422 -4.01371,-3.62462 -18.434454,-10.02614 -25.062776,-2.14159 -0.739902,-4.33703 -1.379051,-14.00491 -0.929399,-19.13498 -1.243009,-12.09377 -1.569182,-17.15845 -3.870857,-37.36866 -0.0491,-5.31038 -0.340626,-10.45913 0.744395,-16.52559 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightThigh\"\n"
        + "     inkscape:connector-curvature=\"0\"\n" + "     sodipodi:nodetypes=\"ccccccccc\" />",
        Constants.BODY_BACK), BACK_RIGHT_LOWER_LEG("bodyPart.back.rightLowerLeg", "<path\n"
        + "     style=\"stroke:#000000;stroke-width:0.7;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;stroke-miterlimit:4;stroke-dasharray:none;\"\n"
        + "     d=\"m 90.956254,309.51523 c 5.245038,-4.53018 11.004166,-7.99791 22.107426,-0.42109 l 1.89492,8.63242 c 0.25396,9.46449 -1.29767,13.51219 -2.10547,19.79141 -1.6206,16.97311 -4.34929,24.61528 -4.84258,40.84609 l 1.26328,3.1582 -1.47383,5.26368 1.47383,8.42187 c -1.61419,-0.19108 -3.22839,-1.13829 -4.84258,-5.26367 l 0.21055,-2.94766 c -3.18515,-6.75881 -5.342174,-7.34887 -7.790234,-9.68515 l -3.158203,2.10547 -0.631641,-2.94766 1.263282,-4.63203 c -1.430024,-9.45446 -3.488973,-19.59093 -4.210938,-29.89766 -1.306705,-6.04577 0.377144,-21.0632 0.842188,-32.42422 z\"\n"
        + "     class=\"shape\" id=\"bodyPart-back-rightLowerLeg\"\n"
        + "     inkscape:connector-curvature=\"0\"\n"
        + "     sodipodi:nodetypes=\"cccccccccccccccc\" />", Constants.BODY_BACK);

    private final String messageCode;
    private final String path;
    private final String imagePath;
    private static final Map<String, BodyPart> stringToEnum = new HashMap<String, BodyPart>();

    static // Initialize map from constant name to enum constant
    {
        for (BodyPart cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    BodyPart(String textValue, String path, String imagePath) {
        this.messageCode = textValue;
        this.path = path;
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public String getPath() {
        return path;
    }

    public String getImagePath() {
        return imagePath;
    }

    public static BodyPart fromString(String textValue) {
        return stringToEnum.get(textValue);
    }

    /**
     * @return Returns a map of all imagePaths as key mapped to the corresponding bodyParts as
     * value.
     */
    public static Map<String, List<BodyPart>> getImagePathBodyPartMap() {
        Map<String, List<BodyPart>> imagePathBodyPartMap = new HashMap<>();

        for (BodyPart bodyPart : BodyPart.values()) {
            if (!imagePathBodyPartMap.containsKey(bodyPart.getImagePath())) {
                List<BodyPart> bodyParts = new ArrayList<BodyPart>();
                bodyParts.add(bodyPart);
                imagePathBodyPartMap.put(bodyPart.getImagePath(), bodyParts);
            } else {
                imagePathBodyPartMap.get(bodyPart.getImagePath()).add(bodyPart);
            }
        }

        return imagePathBodyPartMap;
    }
}
