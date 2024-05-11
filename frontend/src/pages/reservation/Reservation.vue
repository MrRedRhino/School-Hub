<script setup>
import {computed, getCurrentInstance, ref, watch} from "vue";
import {openPopup} from "@/popup.js";
import SeatPopup from "@/pages/reservation/SeatPopup.vue";
import {account, requireAuth} from "@/auth.js";
import PinchZoom from "@/components/PinchZoom.vue";
import SeatLocation from "@/pages/reservation/SeatLocation.vue";

const {appContext} = getCurrentInstance();
const seats = [
  {x: 5, y: 0, angle: 0, location: {row: "R1", seat: 100}},
  {x: 65, y: 0, angle: 0, location: {row: "R1", seat: 101}},
  {x: 125, y: 0, angle: 0, location: {row: "R1", seat: 102}},
  {x: 185, y: 0, angle: 0, location: {row: "R1", seat: 10}},
  {x: -55, y: 0, angle: 0, location: {row: "R1", seat: 10}},
  {x: -115, y: 0, angle: 0, location: {row: "R1", seat: 10}},
  {x: -175, y: 0, angle: 0, location: {row: "R1", seat: 10}},
  {x: -235, y: 0, angle: 0, location: {row: "R1", seat: 10}},

  {x: -25, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: 35, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: 95, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: 155, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: 215, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: -85, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: -145, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: -205, y: 60, angle: 0, location: {row: "R1", seat: 10}},
  {x: -265, y: 60, angle: 0, location: {row: "R1", seat: 10}},

  {x: 5, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: 65, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: 125, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: 185, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: 245, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: -55, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: -115, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: -175, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: -235, y: 120, angle: 0, location: {row: "R1", seat: 10}},
  {x: -295, y: 120, angle: 0, location: {row: "R1", seat: 10}},

  {x: 35, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: 95, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: 155, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: 215, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: 275, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: -85, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: -145, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: -205, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: -265, y: 180, angle: 0, location: {row: "R1", seat: 10}},
  {x: -325, y: 180, angle: 0, location: {row: "R1", seat: 10}},

  {x: 5, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: 65, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: 125, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: 185, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: 245, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: 305, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: -55, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: -115, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: -175, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: -235, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: -295, y: 240, angle: 0, location: {row: "R1", seat: 10}},
  {x: -355, y: 240, angle: 0, location: {row: "R1", seat: 10}},

  {x: -25, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: 35, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: 95, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: 155, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: 215, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: 275, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: 335, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: -85, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: -145, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: -205, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: -265, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: -325, y: 300, angle: 0, location: {row: "R1", seat: 10}},
  {x: -385, y: 300, angle: 0, location: {row: "R1", seat: 10}},

  {x: 5, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: 65, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: 125, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: 185, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: 245, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: 305, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: 365, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: -55, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: -115, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: -175, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: -235, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: -295, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: -355, y: 360, angle: 0, location: {row: "R1", seat: 10}},
  {x: -415, y: 360, angle: 0, location: {row: "R1", seat: 10}},

  {x: -25, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: 35, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: 95, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: 155, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: 215, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: 275, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: 335, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: 395, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: -85, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: -145, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: -205, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: -265, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: -325, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: -385, y: 420, angle: 0, location: {row: "R1", seat: 10}},
  {x: -445, y: 420, angle: 0, location: {row: "R1", seat: 10}},

  {x: 5, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 65, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 125, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 185, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 245, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 305, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 365, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 425, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: 485, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -55, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -115, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -175, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -235, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -295, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -355, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -415, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -475, y: 480, angle: 0, location: {row: "R1", seat: 10}},
  {x: -535, y: 480, angle: 0, location: {row: "R1", seat: 10}},


  {"x": 780, "y": -550, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 741, "y": -504, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 702, "y": -458, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 663, "y": -412, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 624, "y": -366, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 585, "y": -320, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 546, "y": -274, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 507, "y": -228, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 468, "y": -182, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 429, "y": -136, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 826, "y": -511, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 787, "y": -465, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 748, "y": -419, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 709, "y": -373, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 670, "y": -327, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 631, "y": -281, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 592, "y": -235, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 553, "y": -189, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 514, "y": -143, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 475, "y": -97, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 872, "y": -472, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 833, "y": -426, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 794, "y": -380, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 755, "y": -334, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 716, "y": -288, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 677, "y": -242, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 638, "y": -196, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 599, "y": -150, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 560, "y": -104, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 521, "y": -58, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 918, "y": -433, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 879, "y": -387, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 840, "y": -341, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 801, "y": -295, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 762, "y": -249, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 723, "y": -203, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 684, "y": -157, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 645, "y": -111, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 606, "y": -65, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 567, "y": -19, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 528, "y": 27, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 964, "y": -394, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 925, "y": -348, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 886, "y": -302, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 847, "y": -256, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 808, "y": -210, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 769, "y": -164, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 730, "y": -118, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 691, "y": -72, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 652, "y": -26, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 613, "y": 20, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 574, "y": 66, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1010, "y": -355, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 971, "y": -309, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 932, "y": -263, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 893, "y": -217, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 854, "y": -171, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 815, "y": -125, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 776, "y": -79, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 737, "y": -33, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 698, "y": 13, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 659, "y": 59, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 620, "y": 105, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 581, "y": 151, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1056, "y": -316, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1017, "y": -270, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 978, "y": -224, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 939, "y": -178, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 900, "y": -132, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 861, "y": -86, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 822, "y": -40, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 783, "y": 6, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 744, "y": 52, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 705, "y": 98, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 666, "y": 144, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 627, "y": 190, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1102, "y": -277, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1063, "y": -231, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1024, "y": -185, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 985, "y": -139, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 946, "y": -93, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 907, "y": -47, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 868, "y": -1, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 829, "y": 45, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 790, "y": 91, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 751, "y": 137, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 712, "y": 183, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 673, "y": 229, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 634, "y": 275, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1148, "y": -238, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1109, "y": -192, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1070, "y": -146, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1031, "y": -100, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 992, "y": -54, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 953, "y": -8, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 914, "y": 38, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 875, "y": 84, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 836, "y": 130, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 797, "y": 176, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 758, "y": 222, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 719, "y": 268, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 680, "y": 314, "angle": -50, "location": {"seat": 4, "row": "R1"}},


  {"x": 1240, "y": -161, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1201, "y": -115, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1162, "y": -69, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1123, "y": -23, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1084, "y": 23, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1045, "y": 69, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1006, "y": 115, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 967, "y": 161, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 928, "y": 207, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 889, "y": 253, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 850, "y": 299, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 811, "y": 345, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 772, "y": 391, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 733, "y": 437, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 694, "y": 483, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1286, "y": -122, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1247, "y": -76, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1208, "y": -30, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1169, "y": 16, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1130, "y": 62, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1091, "y": 108, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1052, "y": 154, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1013, "y": 200, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 974, "y": 246, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 935, "y": 292, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 896, "y": 338, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 857, "y": 384, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 818, "y": 430, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 779, "y": 476, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 740, "y": 522, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1332, "y": -83, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1293, "y": -37, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1254, "y": 9, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1215, "y": 55, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1176, "y": 101, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1137, "y": 147, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1098, "y": 193, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1059, "y": 239, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1020, "y": 285, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 981, "y": 331, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 942, "y": 377, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 903, "y": 423, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 864, "y": 469, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 825, "y": 515, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 786, "y": 561, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1378, "y": -44, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1339, "y": 2, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1300, "y": 48, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1261, "y": 94, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1222, "y": 140, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1183, "y": 186, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1144, "y": 232, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1105, "y": 278, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1066, "y": 324, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1027, "y": 370, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 988, "y": 416, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 949, "y": 462, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 910, "y": 508, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 871, "y": 554, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1385, "y": 41, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1346, "y": 87, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1307, "y": 133, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1268, "y": 179, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1229, "y": 225, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1190, "y": 271, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1151, "y": 317, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1112, "y": 363, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1073, "y": 409, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1034, "y": 455, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 995, "y": 501, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 956, "y": 547, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 917, "y": 593, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1392, "y": 126, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1353, "y": 172, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1314, "y": 218, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1275, "y": 264, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1236, "y": 310, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1197, "y": 356, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1158, "y": 402, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1119, "y": 448, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1080, "y": 494, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1041, "y": 540, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1002, "y": 586, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 963, "y": 632, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  {"x": 1360, "y": 257, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1321, "y": 303, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1282, "y": 349, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1243, "y": 395, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1204, "y": 441, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1165, "y": 487, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1126, "y": 533, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1087, "y": 579, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1048, "y": 625, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 1009, "y": 671, "angle": -50, "location": {"seat": 4, "row": "R1"}},
  {"x": 970, "y": 717, "angle": -50, "location": {"seat": 4, "row": "R1"}},

  // linke Seite

  {"x": -830, "y": -550, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -791, "y": -504, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -752, "y": -458, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -713, "y": -412, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -674, "y": -366, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -635, "y": -320, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -596, "y": -274, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -557, "y": -228, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -518, "y": -182, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -479, "y": -136, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -876, "y": -511, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -837, "y": -465, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -798, "y": -419, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -759, "y": -373, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -720, "y": -327, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -681, "y": -281, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -642, "y": -235, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -603, "y": -189, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -564, "y": -143, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -525, "y": -97, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -922, "y": -472, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -883, "y": -426, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -844, "y": -380, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -805, "y": -334, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -766, "y": -288, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -727, "y": -242, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -688, "y": -196, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -649, "y": -150, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -610, "y": -104, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -571, "y": -58, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -968, "y": -433, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -929, "y": -387, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -890, "y": -341, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -851, "y": -295, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -812, "y": -249, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -773, "y": -203, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -734, "y": -157, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -695, "y": -111, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -656, "y": -65, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -617, "y": -19, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -578, "y": 27, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1014, "y": -394, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -975, "y": -348, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -936, "y": -302, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -897, "y": -256, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -858, "y": -210, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -819, "y": -164, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -780, "y": -118, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -741, "y": -72, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -702, "y": -26, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -663, "y": 20, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -624, "y": 66, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1060, "y": -355, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1021, "y": -309, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -982, "y": -263, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -943, "y": -217, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -904, "y": -171, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -865, "y": -125, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -826, "y": -79, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -787, "y": -33, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -748, "y": 13, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -709, "y": 59, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -670, "y": 105, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -631, "y": 151, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1106, "y": -316, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1067, "y": -270, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1028, "y": -224, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -989, "y": -178, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -950, "y": -132, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -911, "y": -86, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -872, "y": -40, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -833, "y": 6, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -794, "y": 52, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -755, "y": 98, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -716, "y": 144, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -677, "y": 190, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1152, "y": -277, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1113, "y": -231, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1074, "y": -185, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1035, "y": -139, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -996, "y": -93, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -957, "y": -47, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -918, "y": -1, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -879, "y": 45, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -840, "y": 91, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -801, "y": 137, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -762, "y": 183, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -723, "y": 229, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -684, "y": 275, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1198, "y": -238, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1159, "y": -192, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1120, "y": -146, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1081, "y": -100, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1042, "y": -54, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1003, "y": -8, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -964, "y": 38, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -925, "y": 84, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -886, "y": 130, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -847, "y": 176, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -808, "y": 222, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -769, "y": 268, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -730, "y": 314, "angle": 50, "location": {"seat": 4, "row": "R1"}},


  {"x": -1290, "y": -161, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1251, "y": -115, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1212, "y": -69, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1173, "y": -23, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1134, "y": 23, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1095, "y": 69, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1056, "y": 115, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1017, "y": 161, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -978, "y": 207, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -939, "y": 253, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -900, "y": 299, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -861, "y": 345, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -822, "y": 391, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -783, "y": 437, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -744, "y": 483, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1336, "y": -122, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1297, "y": -76, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1258, "y": -30, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1219, "y": 16, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1180, "y": 62, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1141, "y": 108, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1102, "y": 154, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1063, "y": 200, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1024, "y": 246, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -985, "y": 292, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -946, "y": 338, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -907, "y": 384, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -868, "y": 430, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -829, "y": 476, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -790, "y": 522, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1382, "y": -83, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1343, "y": -37, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1304, "y": 9, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1265, "y": 55, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1226, "y": 101, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1187, "y": 147, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1148, "y": 193, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1109, "y": 239, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1070, "y": 285, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1031, "y": 331, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -992, "y": 377, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -953, "y": 423, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -914, "y": 469, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -875, "y": 515, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -836, "y": 561, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1428, "y": -44, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1389, "y": 2, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1350, "y": 48, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1311, "y": 94, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1272, "y": 140, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1233, "y": 186, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1194, "y": 232, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1155, "y": 278, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1116, "y": 324, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1077, "y": 370, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1038, "y": 416, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -999, "y": 462, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -960, "y": 508, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -921, "y": 554, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1435, "y": 41, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1396, "y": 87, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1357, "y": 133, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1318, "y": 179, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1279, "y": 225, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1240, "y": 271, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1201, "y": 317, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1162, "y": 363, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1123, "y": 409, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1084, "y": 455, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1045, "y": 501, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1006, "y": 547, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -967, "y": 593, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1442, "y": 126, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1403, "y": 172, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1364, "y": 218, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1325, "y": 264, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1286, "y": 310, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1247, "y": 356, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1208, "y": 402, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1169, "y": 448, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1130, "y": 494, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1091, "y": 540, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1052, "y": 586, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1013, "y": 632, "angle": 50, "location": {"seat": 4, "row": "R1"}},

  {"x": -1410, "y": 257, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1371, "y": 303, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1332, "y": 349, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1293, "y": 395, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1254, "y": 441, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1215, "y": 487, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1176, "y": 533, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1137, "y": 579, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1098, "y": 625, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1059, "y": 671, "angle": 50, "location": {"seat": 4, "row": "R1"}},
  {"x": -1020, "y": 717, "angle": 50, "location": {"seat": 4, "row": "R1"}},
]

const mailAddress = ref();
const sendingMail = ref(false);
const invalidMail = ref(false);
const mailMessage = ref();

const reservations = ref({});
const ownReservationsShown = ref(false);
const ownReservations = computed(() => {
  const result = [];
  for (const reservation in reservations.value) {
    if (reservations.value[reservation] === account.value.name) {
      const parts = reservation.split("-");
      result.push({row: parts[0], seat: parts[1]});
    }
  }
  return result;
});

let maxSeats = null;
const maxSeatsReached = computed(() => {
  let count = 0;
  for (const reservation in reservations.value) {
    if (reservations.value[reservation] === account.value.name) {
      count++;
    }
  }
  return maxSeats !== null && count >= maxSeats;
});

let eventSource;
let reconnect = null;

function connect() {
  eventSource = new EventSource("/api/reservations/live");

  eventSource.addEventListener("set_reservations", event => {
    const newSeats = {};
    const data = JSON.parse(event.data);
    for (let reservation of data["reservations"]) {
      newSeats[reservation["seat"]] = reservation["name"];
    }
    reservations.value = newSeats;
    maxSeats = data["max-seats"];
  });

  eventSource.addEventListener("remove_reservation", event => {
    const json = JSON.parse(event.data);
    delete reservations.value[json["seat"]];
  });

  eventSource.addEventListener("add_reservation", event => {
    const newSeat = JSON.parse(event.data);
    reservations.value[newSeat["seat"]] = newSeat["name"];
  });

  clearTimeout(reconnect);
  eventSource.onerror = () => {
    reconnect = setTimeout(connect, 2000);
  };
}

connect();

function reserveSeat(location) {
  requireAuth("einen Platz zu reservieren", appContext, () => {
    openPopup(SeatPopup, appContext, {
      location: location,
      id: getId(location),
      reservations: reservations,
      "max-seats-reached": maxSeatsReached
    });
  });
}

function showReservations() {
  requireAuth("Reservierungen anzuschauen", appContext, () => {
    ownReservationsShown.value = !ownReservationsShown.value;
  });
}

function hideReservations() {
  ownReservationsShown.value = false;
}

function getId(location) {
  return location.row + "-" + location.seat;
}

function getColor(reservation) {
  if (account.value && reservation === account.value.name) {
    return "#ff2046";
  } else if (reservation === undefined) {
    return "#00bf4b";
  }
  return "#B3B3B3";
}

async function sendMail() {
  mailMessage.value = "";
  invalidMail.value = false;
  sendingMail.value = true;
  const response = await fetch("/api/reservations/send-mail?email-address=" + encodeURIComponent(mailAddress.value), {
    method: "POST"
  });
  sendingMail.value = false;


  invalidMail.value = response.status !== 200;
  if (response.status === 200) {
    mailMessage.value = "Mail wurde verschickt";
    mailAddress.value = "";
  } else {
    mailMessage.value = "Ungültige Mail Adresse";
  }
}

watch(mailAddress, () => {
  invalidMail.value = false;
});
</script>

<template>
  <div class="reservations-wrapper" v-click-outside="hideReservations">
    <a @click="showReservations">Meine Reservierungen</a>
    <div class="reservations" v-if="ownReservationsShown">
      <SeatLocation v-for="reservation in ownReservations" :location="reservation"></SeatLocation>
      <h1 v-if="ownReservations.length === 0">Keine Reservierungen</h1>

      <div v-if="ownReservations.length > 0">
        <h1>Als E-Mail senden:</h1>
        <input v-model="mailAddress" :class="{invalid: invalidMail}" type="email" placeholder="E-Mail Adresse">
        <button @click="sendMail" :disabled="sendingMail">Senden</button>
        <h2 v-if="mailMessage">{{ mailMessage }} </h2>
      </div>
    </div>
  </div>

  <div class="plan-wrapper">
    <PinchZoom>
      <div v-for="seat in seats"
           class="seat"
           :style="{transform: `translate(${seat.x}px, ${seat.y}px) rotate(${seat.angle + 'deg'})`, background: getColor(reservations[getId(seat.location)])}"
           @click="reserveSeat(seat.location)">
      </div>
    </PinchZoom>
  </div>
</template>

<style scoped>
a {
  color: cornflowerblue;
  text-decoration: underline;
  cursor: pointer;
  z-index: 9;
  position: relative;
  margin-left: 10px;
  font-size: 18px;
}

.plan-wrapper {
  height: calc(100dvh - 130px);
  background: var(--background-dark);
  margin: 10px;
  border-radius: 20px;
  overflow: hidden;
}

.reservations {
  position: fixed;
  margin-left: 8px;
  margin-top: 4px;
  display: grid;
  z-index: 8;
  background: var(--background-dark);
  box-shadow: -1px -1px 27px 10px rgba(0, 0, 0, 0.5);
  border-radius: 10px;
  padding: 10px 10px;
  row-gap: 10px;
}

.reservations h1 {
  color: var(--text);
  font-size: 18px;
  font-weight: normal;
  margin: 0 0 5px;
}

.reservations h2 {
  color: var(--text);
  font-size: 16px;
  font-weight: normal;
  margin: 5px 0 0 4px;
}

.seat {
  position: absolute;
  width: 50px;
  height: 50px;
  border-top-right-radius: 50%;
  border-top-left-radius: 50%;
  cursor: pointer;
}

.reservations-wrapper {
  margin-bottom: 10px;
}

input {
  height: 34px;
  background: var(--background);
  color: var(--text);
  border-radius: 10px;
  width: 220px;
  font-size: 16px;
  border: none;
  padding: 0 0 0 10px;
}

input.invalid {
  outline: #ff2549 solid 2px;
}

button {
  height: 34px;
  border-radius: 10px;
  border: none;
  background: var(--blue);
  color: var(--text);
  font-size: 16px;
  cursor: pointer;
  margin-left: 5px;
}

button:disabled {
  background: rgba(255, 255, 255, 0.4);
  cursor: default;
}
</style>