# Conclusions

Here I've learned how to access and update private fields through reflection. I understood that, by
using reflection API, we can access and modify data of a class, even though permissions wouldn't
allow it in a normal case scenario. It is not recommended to use reflection due to its high risk and
low performance implication, but if you are a criminal and want to hack some private balances of a
certain object, this is one of the tools you should use.

# How to test

Instantiate ReflectionTestService and call testReflection in order to see the outcome of every class
type instantiation (custom class, primitive, wrapper, complex wrapper class);