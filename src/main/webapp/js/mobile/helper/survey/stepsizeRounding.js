/*
 * Helper function that rounds a number to a given stepsize
 *
 * Number will be floored or ceiled.
 * If the result has more decimals than the given stepsize it will be shortened.
 */
function stepsizeRounding(start,value, stepsize) {
  value = Number(value);
  stepsize = Number(stepsize);
  if (stepsize !== null && stepsize !== 0 && value !== 0) {
    const distanceFromStart = value - start;

    const steps = Math.round(distanceFromStart / stepsize);

    return start + (steps * stepsize);
  }
  return value;
}
